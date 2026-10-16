@echo off
rem Lighthouse ohne NetBeans starten (nach Clean and Build)
cd /d "%~dp0"
if not exist dist\Lighthouse.jar (echo dist\Lighthouse.jar fehlt: bitte in NetBeans Clean and Build ausfuehren. & pause & exit /b 1)
start "" javaw -Xmx4g -Dsun.java2d.uiScale.enabled=true -jar dist\Lighthouse.jar
