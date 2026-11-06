@REM ----------------------------------------------------------------------------
@REM Maven Wrapper for Windows (Standard mvnw.cmd)
@REM ----------------------------------------------------------------------------
@IF "%DEBUG%" == "" @ECHO OFF
@setlocal

set ERROR_CODE=0
@set MAVEN_PROJECTBASEDIR=%~dp0
@set MAVEN_CMD_LINE_ARGS=%*

@REM Try to find system maven first
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
  mvn %*
  goto end
)

echo [INFO] System mvn not found in PATH.
echo [INFO] You can install Maven or run via IDE (IntelliJ IDEA / VS Code / Eclipse).
echo [INFO] To run via Java CLI with maven installed: 'mvn clean spring-boot:run'

:end
@exit /B %ERROR_CODE%
