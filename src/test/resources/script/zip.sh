#!/bin/bash
echo "Installing Node.js..."
export NODE_VERSION=20.14.0
curl -fsSL https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.xz | tar -xJ -C /usr/local --strip-components=1
#!/bin/bash
echo "Installazione dipendenze actionCommon in functionsLib..."
if [ -d ./functionsLib/actionCommon ]; then
  cd ./functionsLib/actionCommon || exit 1
  npm install --production
  cd - || exit 1
  echo "Installazione completata."
else
  echo "Directory ./functionsLib/actionCommon non trovata."
  exit 1
fi

echo "Installazione dipendenze..."
for dir in ./functions/*-manager; do
  if [ -d "$dir" ]; then
    echo "npm install in $(basename "$dir")"
    cd "$dir" || exit 1
    npm install --production
    cd - || exit 1
  fi
done

echo "Zipping tutte le lambda..."
for dir in ./functions/*-manager; do
  if [ -d "$dir" ]; then
    echo "Zipping $(basename "$dir")"
    cd "$dir" || exit 1
    [ -f ./function.zip ] && rm -f ./function.zip
    zip -r ./function.zip . -x './src/test/*' -x '*.md' -x './nodejs/*' -x './.nyc_output/*' -x './.scannerwork/*' -x './coverage/*' -x '*.env' -x '*.zip' -x '*.gitignore'
    cd - || exit 1
  fi
done
echo "Zip completati."