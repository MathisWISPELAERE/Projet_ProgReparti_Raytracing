@echo off
setlocal

REM ============================================================
REM  Configuration
REM  Adaptez ces chemins à votre projet
REM ============================================================

SET PROJECT_DIR=%~dp0
SET SRC_DIR=%PROJECT_DIR%src
SET OUT_DIR=%PROJECT_DIR%bin
SET CLASSPATH=%OUT_DIR%

REM Fichier de description de scène et dimensions
SET SCENE=%SRC_DIR%\simple.txt
SET LARGEUR=4096
SET HAUTEUR=4096

REM ============================================================
REM  Compilation
REM ============================================================

echo [1/3] Compilation...

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" ^
    "%SRC_DIR%\common\Point.java" ^
    "%SRC_DIR%\common\ClientInterface.java" ^
    "%SRC_DIR%\common\ServerInterface.java" ^
    "%SRC_DIR%\server\ServerImpl.java" ^
    "%SRC_DIR%\server\ServerMain.java" ^
    "%SRC_DIR%\client\ClientImpl.java" ^
    "%SRC_DIR%\client\ClientMain.java"

if errorlevel 1 (
    echo ERREUR : la compilation a echoue.
    pause
    exit /b 1
)
echo Compilation OK.

REM ============================================================
REM  Demande du nombre de clients
REM ============================================================

set /p NB_CLIENTS=Combien de clients voulez-vous lancer ? 

if "%NB_CLIENTS%"=="" (
    echo Nombre invalide.
    pause
    exit /b 1
)

REM ============================================================
REM  Lancement du serveur dans une nouvelle fenetre
REM ============================================================

echo [2/3] Lancement du serveur...

start "Serveur RMI" cmd /k "java -cp "%OUT_DIR%" server.ServerMain %SCENE% %LARGEUR% %HAUTEUR%"

REM Attendre que le serveur soit prêt avant de connecter les clients
timeout /t 3 /nobreak >nul



REM ============================================================
REM  Lancement des clients dans des fenetres séparées
REM ============================================================

echo [3/3] Lancement de %NB_CLIENTS% client(s)...

for /L %%i in (1,1,%NB_CLIENTS%) do (
    start "Client %%i" cmd /k "java -cp "%OUT_DIR%" client.ClientMain localhost"
    timeout /t 1 /nobreak >nul
)

echo.
echo Tout est lance !
echo - Allez dans la fenetre "Serveur RMI"
echo - Appuyez sur Entree pour demarrer le rendu une fois les clients connectes.
echo.
pause
