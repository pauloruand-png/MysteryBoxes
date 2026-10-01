@echo off
chcp 65001 > nul
echo ========================================================
echo    Compilando MysteryBoxes (Minecraft 1.21.x)
echo ========================================================
echo.

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O Maven (mvn) nao foi encontrado no seu sistema!
    echo Certifique-se de ter o Maven e o Java 21 instalados.
    echo.
    echo Dica: Voce tambem pode abrir esta pasta diretamente no
    echo IntelliJ IDEA, Eclipse ou VS Code para compilar.
    echo.
    pause
    exit /b
)

echo Executando: mvn clean package...
echo.
call mvn clean package

if %errorlevel% equ 0 (
    echo.
    echo ========================================================
    echo  [SUCESSO] Plugin compilado com sucesso!
    echo  O arquivo .jar esta em: target\MysteryBoxes-1.0.0.jar
    echo ========================================================
) else (
    echo.
    echo [ERRO] Ocorreu uma falha durante a compilacao.
)

echo.
pause
