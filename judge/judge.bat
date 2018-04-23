@echo off
echo Start
cd ..
for %%G in (samples\*.java) do (
  echo ---- Run %%~nG ----
  java -cp bin Main %%~fG
  echo.
)

for %%G in (samples\*.spg) do (
  javac -d judge\tmp %%~pG%%~nG.java
  java -cp judge\tmp %%~nG > judge\goal.txt

  echo ---- sPiglet %%~nG ----
  java -jar judge\spp.jar < %%~fG
  java -jar judge\pgi.jar < %%~fG > judge\spiglet.txt
  fc judge\spiglet.txt judge\goal.txt

  echo ---- Kanga %%~nG ----
  java -jar judge\kgi.jar < %%~pG%%~nG.kg > judge\kanga.txt
  fc judge\kanga.txt judge\goal.txt
  echo.
)

pause

cd judge
del /Q spiglet.txt
del /Q kanga.txt
del /Q goal.txt
del /S /Q tmp
rmdir /Q tmp