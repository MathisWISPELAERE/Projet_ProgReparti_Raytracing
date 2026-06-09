#!/bin/bash
# ============================================================
#  Configuration
#  Adaptez ces chemins à votre projet
# ============================================================
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/bin"
CLASSPATH="$OUT_DIR"

# Fichier de description de scène et dimensions
SCENE="$SRC_DIR/simple.txt"
LARGEUR=4096
HAUTEUR=4096

# ============================================================
#  Compilation
# ============================================================
echo "[1/3] Compilation..."
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
  echo "ERREUR : la compilation a echoue."
  exit 1
fi
echo "Compilation OK."

# ============================================================
#  Demande du nombre de clients
# ============================================================
read -p "Combien de clients voulez-vous lancer ? " NB_CLIENTS

if [ -z "$NB_CLIENTS" ] || ! [[ "$NB_CLIENTS" =~ ^[0-9]+$ ]]; then
  echo "Nombre invalide."
  exit 1
fi

# ============================================================
#  Lancement du serveur dans un nouveau terminal
# ============================================================
echo "[2/3] Lancement du serveur..."

if command -v gnome-terminal &>/dev/null; then
  gnome-terminal --title="Serveur RMI" -- bash -c "java -cp \"$OUT_DIR\" server.ServerMain \"$SCENE\" $LARGEUR $HAUTEUR; exec bash"
elif command -v xterm &>/dev/null; then
  xterm -title "Serveur RMI" -e bash -c "java -cp \"$OUT_DIR\" server.ServerMain \"$SCENE\" $LARGEUR $HAUTEUR; exec bash" &
elif command -v konsole &>/dev/null; then
  konsole --title "Serveur RMI" -e bash -c "java -cp \"$OUT_DIR\" server.ServerMain \"$SCENE\" $LARGEUR $HAUTEUR; exec bash" &
else
  echo "Aucun terminal graphique détecté. Lancement du serveur en arrière-plan..."
  java -cp "$OUT_DIR" server.ServerMain "$SCENE" $LARGEUR $HAUTEUR &
fi

# Attendre que le serveur soit prêt avant de connecter les clients
sleep 3

# ============================================================
#  Lancement des clients dans des terminaux séparés
# ============================================================
echo "[3/3] Lancement de $NB_CLIENTS client(s)..."

for ((i=1; i<=NB_CLIENTS; i++)); do
  if command -v gnome-terminal &>/dev/null; then
    gnome-terminal --title="Client $i" -- bash -c "java -cp \"$OUT_DIR\" client.ClientMain localhost; exec bash"
  elif command -v xterm &>/dev/null; then
    xterm -title "Client $i" -e bash -c "java -cp \"$OUT_DIR\" client.ClientMain localhost; exec bash" &
  elif command -v konsole &>/dev/null; then
    konsole --title "Client $i" -e bash -c "java -cp \"$OUT_DIR\" client.ClientMain localhost; exec bash" &
  else
    java -cp "$OUT_DIR" client.ClientMain localhost &
  fi
  sleep 1
done

echo ""
echo "Tout est lancé !"
echo "- Allez dans la fenêtre \"Serveur RMI\""
echo "- Appuyez sur Entrée pour démarrer le rendu une fois les clients connectés."
echo ""
read -p "Appuyez sur Entrée pour quitter..."