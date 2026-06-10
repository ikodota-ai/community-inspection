# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

RuoYi (若依) v3.9.2 — a rapid development platform with Spring Boot 4.x (Java 17) backend and Vue 2.6 + Element UI frontend. This is a multi-module Maven project with backend modules under `ruoyi-*` and the frontend under `ruoyi-ui`.

## Build & Run Commands

**Backend (Maven, Java 17):**
- `mvn clean package` — build all modules
- `mvn spring-boot:run` — run from root (uses `ruoyi-admin` as entry point)
- `java -jar ruoyi-admin/target/ruoyi-admin-3.9.2.jar` — run packaged jar

**Frontend (Vue CLI 4):**
- `cd ruoyi-ui && npm run dev` — development server on port 80, proxying `/dev-api` → `http://localhost:8080`
- `cd ruoyi-ui && npm run build:prod` — production build
- `cd ruoyi-ui && npm run build:stage` — staging build

**Database:** MySQL database named `ry-vue`. SQL init scripts in `sql/` (run `ry_20260417.sql` then `quartz.sql`). Default Druid monitor at `/druid/*` (login: ruoyi/123456).

## Module Architecture

| Module | Role |
|---|---|
| `ruoyi-admin` | Spring Boot entry point (`RuoYiApplication`). Contains all MVC controllers under `com.ruoyi.web.controller`, organized by `system/`, `monitor/`, `common/`, `tool/`. |
| `ruoyi-common` | Shared foundation: base classes (`BaseController`, `BaseEntity`, `TreeEntity`, `AjaxResult`, `R`, `TableDataInfo`), enums, annotations (`@Log`, `@DataScope`, `@DataSource`, `@RateLimiter`, `@RepeatSubmit`, `@Anonymous`), exception hierarchy, XSS filter, utility classes. |
| `ruoyi-framework` | Cross-cutting infrastructure: Spring Security config with JWT stateless auth, dynamic DataSource switching via AOP, aspect-oriented logging/data-scope/rate-limiting, Redis config, Druid config, global exception handler, `PermissionService` (the runtime permission-checking utility used in templates with `@ss`). |
| `ruoyi-system` | Core business domain: entities, MyBatis mappers (XML in `resources/mapper/system/`), and service interfaces/implementations for User, Role, Menu, Dept, Post, Dict, Config, Notice, OperLog, Logininfor. |
| `ruoyi-quartz` | Scheduled task (cron job) management using Quartz scheduler. Task definitions stored in DB (`sys_job` table). |
| `ruoyi-generator` | Code generation module. Reads DB table metadata, renders Java/Vue/SQL/XML via Velocity templates. Can generate complete CRUD for a table. |
| `ruoyi-ui` | Vue 2 frontend. Axios-based API layer (`src/api/`), Vuex stores (`src/store/modules/`), dynamic permission-based routing (`src/permission.js`), layout with sidebar/topbar. |

**Dependency chain:** `admin` → `framework` → `system` → `common`; `quartz` and `generator` depend on `common`. All six are declared as `<module>` in the root POM.

## Key Architecture Patterns

### Request Flow
`Vue request` → `JwtAuthenticationTokenFilter` (extracts token, sets `Authentication`) → `SecurityFilterChain` (authorizes URL) → Controller → `@PreAuthorize` method-level check via `PermissionService.hasPermi()` → Service → Mapper/XML → MySQL

### Authentication & Authorization
- Stateless JWT: token stored in `Authorization: Bearer <token>` header, configured via `token.header` in application.yml
- `TokenService` creates/validates tokens (Redis-backed for online user tracking)
- `UserDetailsServiceImpl` loads user/roles/permissions from DB
- URLs allowed anonymously are declared via `@Anonymous` annotation on controllers (collected into `PermitAllUrlProperties` at startup)
- Permission checks: `@PreAuthorize("@ss.hasPermi('system:user:list')")` or `@PreAuthorize("@ss.hasRole('admin')")`, where `ss` is the bean name of `PermissionService`

### Response Format
All API responses use `AjaxResult`:
- Success: `{ code: 200, msg: "操作成功", data: ... }`
- Table data: `TableDataInfo` wraps rows + total for pagination
- Tree data: `{ code: 200, data: [...] }` (list of `TreeSelect`)

### Data Permissions (DataScope)
`@DataScope` annotation on Mapper methods triggers `DataScopeAspect`. It modifies SQL to filter by `dept_id` based on the user's role data scope (all data / custom / dept only / dept and children / self only).

### Multi-DataSource
`@DataSource(DataSourceType.SLAVE)` on Service/Mapper methods switches the datasource via `DynamicDataSourceContextHolder` + AOP. Master is the default; slave must be enabled in `application-druid.yml`.

### Frontend Dynamic Routes
Menu routes are fetched from backend (`getRouters` API). The backend returns menu tree with `component` strings like `"system/user/index"`. The frontend `filterAsyncRouter` resolves these to actual Vue component objects via dynamic `import()` or `require()`. Permission checks happen both server-side (routing) and client-side (`auth.hasPermi()` directive).

### Key Annotations (declared in `ruoyi-common`)
- `@Anonymous` — skip authentication
- `@Log(title, businessType, operatorType)` — record operation log (consumed by `LogAspect`)
- `@DataScope(deptAlias, userAlias)` — apply data permission filtering to SQL
- `@DataSource(DataSourceType)` — switch datasource
- `@RateLimiter(count, time, limitType)` — rate limiting
- `@RepeatSubmit(interval)` — prevent duplicate form submission
- `@Excel(name)` — Excel import/export field mapping
- `@Sensitive(DesensitizedType)` — data masking

### Common Base Classes
- `BaseController` — provides `startPage()`, `getDataTable()`, `success()`, `error()` helpers
- `BaseEntity` — common entity fields: `createBy`, `createTime`, `updateBy`, `updateTime`, `remark`, `params`
- `TreeEntity` — extends `BaseEntity` with tree fields: `parentId`, `parentName`, `ancestors`, `orderNum`, `children`

## Configuration Files

- `ruoyi-admin/src/main/resources/application.yml` — main config (server, Redis, token, MyBatis, captcha type, password policy, XSS settings)
- `ruoyi-admin/src/main/resources/application-druid.yml` — datasource config (master/slave, Druid pool settings)
- `ruoyi-ui/vue.config.js` — devServer proxy config (`/dev-api` → `http://localhost:8080`), webpack chunk splitting
- `ruoyi-ui/.env.development` / `.env.production` / `.env.staging` — frontend environment variables including `VUE_APP_BASE_API`

## Important Conventions

- Service interfaces are named `ISysXxxService`, implementations `SysXxxServiceImpl`
- Mapper XML files live under `resources/mapper/` in each module (e.g., `mapper/system/SysUserMapper.xml`)
- Roles/permissions are stored as comma-separated strings in the database (e.g., `role_key: "admin"`, `perms: "system:user:list,system:user:add"`)
- Menu types: `M` (directory), `C` (menu), `F` (button)
- The `params` map on entities (inherited from `BaseEntity`) is used to pass extra request parameters that don't map to entity fields
