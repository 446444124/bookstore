<template>
  <router-view />
</template>

<style>
:root {
  --admin-bg: #f6f8fb;
  --admin-surface: #ffffff;
  --admin-text: #0f172a;
  --admin-sub: #64748b;
  --admin-border: #e7edf4;
  --admin-primary: #4f46e5;
  --admin-primary-soft: #ececff;
  --admin-success: #3f8f6b;
  --admin-warning: #b7823a;
  --admin-danger: #c85c5c;
  --admin-radius: 8px;
  --admin-space-1: 8px;
  --admin-space-2: 16px;
  --admin-space-3: 24px;
  --admin-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);

  --el-color-primary: var(--admin-primary);
  --el-color-success: var(--admin-success);
  --el-color-warning: var(--admin-warning);
  --el-color-danger: var(--admin-danger);
}

html {
  scroll-behavior: smooth;
}

body {
  margin: 0;
  font-family: Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
  background: var(--admin-bg);
  color: var(--admin-text);
}

* {
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  background: var(--admin-bg);
}

.el-card {
  border-radius: var(--admin-radius);
  border: 1px solid var(--admin-border);
  box-shadow: var(--admin-shadow);
}

.el-button {
  border-radius: var(--admin-radius);
  height: 36px;
  padding: 0 14px;
  font-weight: 600;
  transition: transform .2s ease, box-shadow .2s ease, background-color .2s ease;
}

.el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: var(--admin-radius);
}

.el-input__wrapper,
.el-select__wrapper {
  min-height: 36px;
}

.el-form-item {
  margin-bottom: var(--admin-space-2);
}

.el-form-item__label {
  color: var(--admin-sub);
  font-size: 13px;
  font-weight: 500;
}

.el-table {
  --el-table-header-bg-color: #f7f9fd;
  --el-table-row-hover-bg-color: #f8faff;
  --el-table-border-color: var(--admin-border);
  border-radius: var(--admin-radius);
  overflow: hidden;
}

.el-table th.el-table__cell {
  padding: 10px 0;
}

.el-table td.el-table__cell {
  padding: 11px 0;
}

.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #fcfdff;
}

.title-bar {
  min-height: 56px;
  margin-bottom: var(--admin-space-2);
  border-bottom: 1px solid var(--admin-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-bar .title,
.title-row .title,
.card-head {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--admin-text);
}
</style>
