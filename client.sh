#!/bin/bash
# ============================================================
#  Script de lancement d'un client unique
#  Usage : ./launch_client.sh <IP_SERVEUR>
# ============================================================

# ============================================================
#  Vérification du paramètre IP
# ============================================================
if [ -z "$1" ]; then
    echo "Usage : $0 <IP_SERVEUR>"
    echo "Exemple : $0 192.168.1.42"
    exit 1
fi

SERVER_IP="$1"

# ============================================================
#  Configuration
# ============================================================
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/bin"

# ============================================================
#  Compilation
# ============================================================
echo "[1/2] Compilation..."
mkdir -p "$OUT_DIR"
javac -d "$OUT_DIR" -sourcepath "$SRC_DIR" \
    "$SRC_DIR/common/Point.java" \
    "$SRC_DIR/common/ClientInterface.java" \
    "$SRC_DIR/common/ServerInterface.java" \
    "$SRC_DIR/server/ServerImpl.java" \
    "$SRC_DIR/server/ServerMain.java" \
    "$SRC_DIR/client/ClientImpl.java" \
    "$SRC_DIR/client/ClientMain.java"

if [ $? -ne 0 ]; then
    echo "ERREUR : la compilation a échoué."
    exit 1
fi
echo "Compilation OK."

# ============================================================
#  Lancement du client
# ============================================================
echo "[2/2] Lancement du client vers le serveur : $SERVER_IP"

if command -v gnome-terminal &>/dev/null; then
    gnome-terminal --title="Client RMI -> $SERVER_IP" -- bash -c "java -cp \"$OUT_DIR\" client.ClientMain \"$SERVER_IP\"; exec bash"
elif command -v xterm &>/dev/null; then
    xterm -title "Client RMI -> $SERVER_IP" -e bash -c "java -cp \"$OUT_DIR\" client.ClientMain \"$SERVER_IP\"; exec bash" &
elif command -v konsole &>/dev/null; then
    konsole --title "Client RMI -> $SERVER_IP" -e bash -c "java -cp \"$OUT_DIR\" client.ClientMain \"$SERVER_IP\"; exec bash" &
else
    echo "Aucun terminal graphique détecté. Lancement en arrière-plan..."
    java -cp "$OUT_DIR" client.ClientMain "$SERVER_IP" &
fi

echo ""
echo "Client lancé et connecté à $SERVER_IP !"