@echo off

set jar_bin="upload-package-forios.jar"
if "%1" == "" (
    java -jar %jar_bin%
) else (
    java -jar %jar_bin% %1 %2 %3 %4 %5 %6 %7 %8 %9
)

pause