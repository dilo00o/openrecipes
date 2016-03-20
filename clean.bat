@echo off

if exist ".classpath"         del /Q .classpath
if exist ".project"           del /Q .project
if exist "openrecipes.iml"    del /Q openrecipes.iml
if exist ".cache-main"        del /Q .cache-main
if exist ".cache-tests"       del /Q .cache-tests

if exist ".settings"          rmdir /S /Q .settings
if exist ".target"            rmdir /S /Q .target
if exist ".idea"              rmdir /S /Q .idea
if exist "target"             rmdir /S /Q target
if exist "bin"                rmdir /S /Q bin
if exist "logs"               rmdir /S /Q logs
if exist "project\target"     rmdir /S /Q project\target
if exist "project\project"    rmdir /S /Q project\project
if exist ".idea_modules"      rmdir /S /Q .idea_modules