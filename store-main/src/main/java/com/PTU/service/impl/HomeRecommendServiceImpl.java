package com.PTU.service.impl;

import com.PTU.entity.Book;
import com.PTU.entity.Category;
import com.PTU.entity.Major;
import com.PTU.entity.User;
import com.PTU.mapper.BookMapper;
import com.PTU.mapper.CategoryMapper;
import com.PTU.mapper.MajorMapper;
import com.PTU.mapper.RecommendMapper;
import com.PTU.mapper.UserMapper;
import com.PTU.service.HomeRecommendService;
import com.PTU.vo.HomeRecommendVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HomeRecommendServiceImpl implements HomeRecommendService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private RecommendMapper recommendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MajorMapper majorMapper;

    @Override
    public HomeRecommendVO recommendForHome(Long userId, int categoryLimit, int bookLimit) {
        int catLimit = Math.max(1, Math.min(categoryLimit, 20));
        int bkLimit = Math.max(1, Math.min(bookLimit, 30));

        List<Category> enabledCategories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getName));
        Map<Long, Category> categoryMap = enabledCategories.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a, LinkedHashMap::new));

        List<Book> books = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1)
                .gt(Book::getStock, 0)
                .orderByDesc(Book::getUpdateTime));
        Map<Long, Book> bookMap = books.stream()
                .filter(b -> b.getId() != null)
                .collect(Collectors.toMap(Book::getId, b -> b, (a, b) -> a));

        String majorName = resolveMajorName(userId);
        Set<String> majorKeywords = extractKeywords(majorName);

        Map<Long, Double> categoryScore = new HashMap<>();
        Map<Long, Double> bookScore = new HashMap<>();

        if (userId != null) {
            for (Map<String, Object> row : recommendMapper.userCategoryPref(userId, 20)) {
                Long cid = toLong(row.get("categoryId"));
                long qty = toLongVal(row.get("qty"));
                if (cid != null) addScore(categoryScore, cid, qty * 5.0);
            }
            for (Map<String, Object> row : recommendMapper.userBookPref(userId, 30)) {
                Long bid = toLong(row.get("bookId"));
                long qty = toLongVal(row.get("qty"));
                if (bid != null) addScore(bookScore, bid, qty * 8.0);
            }
        }

        for (Map<String, Object> row : recommendMapper.globalCategoryHot(30)) {
            Long cid = toLong(row.get("categoryId"));
            long qty = toLongVal(row.get("qty"));
            if (cid != null) addScore(categoryScore, cid, qty * 1.0);
        }
        for (Map<String, Object> row : recommendMapper.globalBookHot(80)) {
            Long bid = toLong(row.get("bookId"));
            long qty = toLongVal(row.get("qty"));
            if (bid != null) addScore(bookScore, bid, qty * 1.0);
        }

        for (Category c : enabledCategories) {
            Long cid = c.getId();
            if (cid == null) continue;
            if (matchesAny(c.getName(), majorKeywords)) {
                addScore(categoryScore, cid, 30.0);
            }
        }

        for (Book b : books) {
            Long bid = b.getId();
            if (bid == null) continue;
            if (b.getCategoryId() != null) {
                addScore(bookScore, bid, categoryScore.getOrDefault(b.getCategoryId().longValue(), 0.0) * 0.2);
            }
            if (matchesAny(bookText(b), majorKeywords)) {
                addScore(bookScore, bid, 20.0);
            }
        }

        List<Category> hotCategories = categoryScore.entrySet().stream()
                .filter(e -> categoryMap.containsKey(e.getKey()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> categoryMap.get(e.getKey()))
                .distinct()
                .limit(catLimit)
                .collect(Collectors.toList());
        if (hotCategories.size() < catLimit) {
            for (Category c : enabledCategories) {
                if (hotCategories.size() >= catLimit) break;
                if (!hotCategories.contains(c)) hotCategories.add(c);
            }
        }

        Set<Long> preferCategoryIds = hotCategories.stream().map(Category::getId).collect(Collectors.toSet());
        List<Book> featuredBooks = bookScore.entrySet().stream()
                .filter(e -> bookMap.containsKey(e.getKey()))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(e -> bookMap.get(e.getKey()))
                .filter(b -> b.getCategoryId() == null || preferCategoryIds.isEmpty() || preferCategoryIds.contains(b.getCategoryId().longValue()))
                .distinct()
                .limit(bkLimit)
                .collect(Collectors.toList());
        if (featuredBooks.size() < bkLimit) {
            List<Book> fallback = new ArrayList<>(books);
            fallback.sort(Comparator.comparing(Book::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            for (Book b : fallback) {
                if (featuredBooks.size() >= bkLimit) break;
                if (!featuredBooks.contains(b)) featuredBooks.add(b);
            }
        }

        return HomeRecommendVO.builder()
                .hotCategories(hotCategories)
                .featuredBooks(featuredBooks)
                .build();
    }

    private String resolveMajorName(Long userId) {
        if (userId == null) return "";
        User user = userMapper.selectById(userId);
        if (user == null || user.getMajorId() == null) return "";
        Major major = majorMapper.selectById(user.getMajorId());
        return major == null || major.getName() == null ? "" : major.getName();
    }

    private Set<String> extractKeywords(String majorName) {
        if (majorName == null || majorName.trim().isEmpty()) return new HashSet<>();
        String s = majorName.trim().toLowerCase(Locale.ROOT);
        Set<String> set = new HashSet<>();
        set.add(s);
        set.addAll(Arrays.stream(s.split("[\\s,;，；/]+"))
                .map(String::trim)
                .filter(v -> v.length() >= 2)
                .collect(Collectors.toSet()));
        return set;
    }

    private boolean matchesAny(String text, Set<String> kws) {
        if (text == null || text.trim().isEmpty() || kws == null || kws.isEmpty()) return false;
        String t = text.toLowerCase(Locale.ROOT);
        for (String kw : kws) {
            if (kw != null && !kw.isEmpty() && t.contains(kw)) return true;
        }
        return false;
    }

    private String bookText(Book b) {
        return String.valueOf(b.getTitle()) + " " + String.valueOf(b.getAuthor()) + " " + String.valueOf(b.getDescription());
    }

    private void addScore(Map<Long, Double> score, Long id, double val) {
        score.put(id, score.getOrDefault(id, 0.0) + val);
    }

    private Long toLong(Object o) {
        try {
            if (o == null) return null;
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private long toLongVal(Object o) {
        Long v = toLong(o);
        return v == null ? 0L : v;
    }
}
