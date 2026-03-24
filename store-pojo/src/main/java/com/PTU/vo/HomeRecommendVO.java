package com.PTU.vo;

import com.PTU.entity.Category;
import com.PTU.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeRecommendVO implements Serializable {
    private List<Category> hotCategories;
    private List<Book> featuredBooks;
}
