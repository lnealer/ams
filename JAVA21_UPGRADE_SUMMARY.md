# Java 21 Upgrade Summary

## Overview
This document summarizes the Java 21 upgrade for the AMS (Asset Management System) codebase, including completed changes and remaining work.

## Completed Changes

### 1. **POM Configuration Updates** ✅
- **Maven Compiler**: Updated `maven.compiler.source` and `maven.compiler.target` from `1.8` to `21`
- **Maven Compiler Plugin**: Added explicit version `3.13.0` with Java 21 release configuration
- **Maven Surefire Plugin**: Updated to version `3.2.5` for Java 21 compatibility

### 2. **Framework Upgrades** ✅
- **Struts 2**: `6.8.0` → `7.0.0` (Jakarta EE 9+ compatible)
- **Spring Framework**: `5.3.39` → `6.1.13` (Jakarta EE 9+ compatible)
- **Spring Security**: `5.3.13.RELEASE` → `6.2.10.RELEASE` (Jakarta EE 9+ compatible)
- **OGNL**: `3.3.5` → `3.4.3`
- **AspectJ**: `1.7.4` → `1.9.22`

### 3. **Jakarta EE 9+ Migration** ✅
Replaced all `javax.*` namespaces with `jakarta.*`:
- **Servlet API**: `javax.servlet:javax.servlet-api:3.1.0` → `jakarta.servlet:jakarta.servlet-api:6.0.0`
- **Annotation API**: `javax.annotation:javax.annotation-api:1.3.2` → `jakarta.annotation:jakarta.annotation-api:2.1.1`
- **JSTL**: `jstl:jstl:1.2` → `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.1` + `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`

### 4. **Test Framework Upgrades** ✅
- **JUnit**: `4.12` → `5.10.2` (JUnit Jupiter)
- **JUnit Platform**: Added `1.10.2`
- **Mockito**: `1.9.5` → `5.7.0` (modern version with Java 21 support)

### 5. **Database & Utilities** ✅
- **Oracle JDBC**: Updated to `23.8.0.25.04` (matches Oracle 23ai server)
- **H2 Database**: `1.3.176` → `2.2.224` (for testing)
- **JaCoCo**: `0.8.5` → `0.8.11` (Java 21 support)

### 6. **Container Configuration** ✅
- **Dockerfile**: Updated to use `websphere-liberty:latest-java21-full` image
- **Liberty Features**: Updated for Jakarta EE 9+:
  - `servlet-3.1` → `servlet-6.0`
  - `jsp-2.3` → `pages-3.1`
  - `jdbc-4.1` → `jdbc-4.3`
- **Platform Support**: Removed `linux/amd64` pin (Java 21 Liberty supports both amd64 and arm64 natively)

### 7. **Web Configuration** ✅
- **web.xml**: Updated namespace from `http://xmlns.jcp.org/xml/ns/javaee` to `https://jakarta.ee/xml/ns/jakartaee`
- **web.xml**: Updated schema version from `3.1` to `6.0`
- **server.xml**: Updated Liberty features for Jakarta EE 9+

### 8. **Module POMs** ✅
- **AssetManagementSharedServices**: Updated to use `jakarta.servlet-api`
- **AssetManagementInternalWeb**: Updated to use Jakarta EE 9+ dependencies

## Remaining Work

### 1. **Test Code Migration** ⚠️ CRITICAL
All test files need to be migrated from JUnit 4 to JUnit 5 syntax:

**Current Issues:**
- `@Before` → `@BeforeEach`
- `@Test` → `@Test` (same, but needs import from `org.junit.jupiter.api`)
- Static imports from `org.junit.Assert.*` → `org.junit.jupiter.api.Assertions.*`
- `@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`
- `org.mockito.Matchers` → `org.mockito.ArgumentMatchers`

**Affected Test Files (5 files in AssetManagementSharedCommon):**
- `AddressTest.java`
- `AssetTest.java`
- `LoadableTypeTest.java`
- `OrderTest.java`
- `LegacyAssetTagComparatorTest.java`

**Plus additional test files in other modules that will need similar updates.**

### 2. **Source Code Updates** ⚠️ IMPORTANT
Based on TECH_STACK.md patterns, the following source code updates are needed:

**Struts 7 Parameter Binding:**
- Add `@StrutsParameter` annotations to all action setter methods
- Struts 7 no longer injects request parameters into unannotated setters
- **Pattern**: 53 files with `com.opensymphony.*` imports need review

**Spring Annotation Updates:**
- Review 91 files with `@Autowired` field injection
- Consider migrating to constructor injection (best practice for Java 21)
- Update `@EnableGlobalMethodSecurity(jsr250Enabled = true)` if using JSR-250 annotations

**Date/Time Modernization:**
- Replace `Calendar` and `new Date()` (17 files) with `java.time` API
- Replace `SimpleDateFormat` (4 files) with `java.time.format.DateTimeFormatter`

### 3. **Compilation Verification** ⏳ PENDING
Once test files are migrated:
```bash
mvn clean compile
mvn clean test
```

### 4. **Integration Testing** ⏳ PENDING
- Verify all modules compile successfully
- Run full test suite
- Test with Liberty Java 21 container
- Verify database connectivity with Oracle 23ai

### 5. **Documentation Updates** ⏳ PENDING
- Update TECH_STACK.md with new versions
- Update build scripts if needed
- Document any breaking changes for developers

## Migration Path Summary

The upgrade follows the documented dependency chain from TECH_STACK.md:

1. ✅ **Jakarta EE 9+** (servlet-3.1 → servlet-6.0, jsp-2.3 → pages-3.1)
2. ✅ **Spring 6.x** (requires Jakarta EE 9+)
3. ✅ **Spring Security 6.x** (requires Spring 6)
4. ✅ **Struts 7.x** (requires Jakarta EE 9+)
5. ✅ **Java 21** (runtime and compiler)
6. ⏳ **Test Framework Migration** (JUnit 4 → JUnit 5)
7. ⏳ **Source Code Updates** (Struts 7 parameter binding, Spring best practices, Java 8 → Java 21 APIs)

## Key Benefits of Java 21

- **Performance**: Improved garbage collection and memory management
- **Virtual Threads**: Foundation for high-concurrency applications (future enhancement)
- **Pattern Matching**: Simplified complex conditional logic
- **Records**: Cleaner data classes (future enhancement)
- **Sealed Classes**: Better type safety (future enhancement)
- **Modern Libraries**: Access to latest Spring, Struts, and Jakarta EE features

## Known Issues & Workarounds

### 1. Mockito Version
- Using Mockito 5.7.0 (latest stable for Java 21)
- Removed `mockito-all` in favor of `mockito-core` + `mockito-junit-jupiter`

### 2. Liberty Image
- Using `websphere-liberty:latest-java21-full` (latest available)
- Removed `linux/amd64` platform pin (Java 21 supports both architectures)
- Reduced health check start-period from 180s to 60s (Java 21 starts faster)

### 3. Oracle JDBC
- Runtime driver: `ojdbc8-23.8.0.25.04` (matches Oracle 23ai)
- Test driver: `ojdbc8-23.8.0.25.04` (updated from 12.2.0.1)
- Both versions support Java 21

## Next Steps

1. **Immediate**: Migrate all test files from JUnit 4 to JUnit 5
2. **Short-term**: Update source code for Struts 7 parameter binding
3. **Medium-term**: Modernize date/time handling and Spring configuration
4. **Long-term**: Leverage Java 21 features (virtual threads, pattern matching, records)

## Testing Checklist

- [ ] All modules compile without errors
- [ ] All unit tests pass
- [ ] Integration tests pass
- [ ] Container builds successfully
- [ ] Application starts in Liberty
- [ ] Database connectivity verified
- [ ] All endpoints functional
- [ ] Performance benchmarks (optional)

## References

- [Java 21 Release Notes](https://www.oracle.com/java/technologies/javase/21-relnotes.html)
- [Jakarta EE 9 Migration Guide](https://jakarta.ee/learn/migration/)
- [Spring 6 Migration Guide](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-6.0-Release-Notes)
- [Struts 7 Migration Guide](https://struts.apache.org/docs/release-notes-7-0-0.html)
- [JUnit 5 Migration Guide](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4)
