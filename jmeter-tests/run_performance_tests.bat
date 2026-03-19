@echo off

IF "%JMETER_HOME%"=="" SET "JMETER_HOME=%APPDATA%\JetBrains\IntelliJIdea2025.3\apache-jmeter-5.6.3"
SET "JMETER_CMD=%JMETER_HOME%\bin\jmeter.bat"
SET "TEST_PLAN=%~dp0rural_animal_performance_test.jmx"
SET "RESULTS_DIR=%~dp0results"
SET "REPORT_DIR=%~dp0html-report"
SET "BASE_URL=localhost"
SET "PORT=8080"
SET "PROTOCOL=http"
SET "THREADS=200"
SET "RAMP_UP=10"
SET "LOOPS=5"
echo === Rural Animal - Pruebas de Rendimiento ===
echo Servidor: %PROTOCOL%://%BASE_URL%:%PORT%
echo Hilos: %THREADS% Ramp-Up: %RAMP_UP%s Loops: %LOOPS%
IF NOT EXIST "%JMETER_CMD%" (
    echo [ERROR] JMeter no encontrado en: %JMETER_CMD%
    echo Descarga: https://jmeter.apache.org/download_jmeter.cgi
    pause
    exit /b 1
)
echo [1/3] Limpiando resultados anteriores...
IF EXIST "%RESULTS_DIR%" rmdir /s /q "%RESULTS_DIR%"
IF EXIST "%REPORT_DIR%" rmdir /s /q "%REPORT_DIR%"
mkdir "%RESULTS_DIR%"
echo [2/3] Ejecutando pruebas...
call "%JMETER_CMD%" -n -t "%TEST_PLAN%" -l "%RESULTS_DIR%\results.jtl" -j "%RESULTS_DIR%\jmeter.log" -Jbase_url=%BASE_URL% -Jport=%PORT% -Jprotocol=%PROTOCOL% -Jthreads=%THREADS% -Jramp_up=%RAMP_UP% -Jloops=%LOOPS% -e -o "%REPORT_DIR%"
echo [3/3] Abriendo reporte HTML...
IF EXIST "%REPORT_DIR%\index.html" start "" "%REPORT_DIR%\index.html"
echo === Finalizado ===
pause
