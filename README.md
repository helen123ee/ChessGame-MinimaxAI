Para executar, cole no terminal:

(base) PS C:\Users\Thayse\Desktop\POO\ChessGame> Remove-Item -Recurse -Force .\out -ErrorAction SilentlyContinue

 New-Item -ItemType Directory -Force .\out | Out-Null

$files = Get-ChildItem -Recurse -Path .\src -Filter *.java | ForEach-Object FullName

javac -Xlint:all -encoding UTF-8 -d out $files

java -cp "out;resources" view.ChessGUI
