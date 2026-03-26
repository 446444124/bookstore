<template>
  <router-view />
</template>

<style>
:root {
  --font-heading: "Rubik", ui-sans-serif, system-ui, sans-serif;
  --font-body: "Nunito Sans", ui-sans-serif, system-ui, "PingFang SC", "Microsoft YaHei", sans-serif;

  --admin-bg: #f8fafc;
  --admin-surface: #ffffff;
  --admin-text: #0f172a;
  --admin-sub: #475569;
  --admin-border: #e2e8f0;
  --admin-primary: #0369a1;
  --admin-primary-soft: #e0f2fe;
  --admin-success: #0d9488;
  --admin-warning: #b45309;
  --admin-danger: #b91c1c;
  --admin-radius: 10px;
  --admin-space-1: 8px;
  --admin-space-2: 16px;
  --admin-space-3: 24px;
  --admin-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  --admin-shadow-sm: 0 1px 2px rgba(15, 23, 42, 0.05);

  --el-color-primary: var(--admin-primary);
  --el-color-success: var(--admin-success);
  --el-color-warning: var(--admin-warning);
  --el-color-danger: var(--admin-danger);
}

html {
  scroll-behavior: smooth;
}

@media (prefers-reduced-motion: reduce) {
  html {
    scroll-behavior: auto;
  }
}

body {
  margin: 0;
  font-family: var(--font-body);
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
  cursor: pointer;
  transition: box-shadow 0.2s ease, background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.el-button:hover {
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

@media (prefers-reduced-motion: reduce) {
  .el-button {
    transition: none;
  }
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
  font-family: var(--font-heading);
  font-size: 22px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--admin-text);
}
</style>
