@echo off
echo Start
cd ..
for %%G in (samples\*.java) do (
  echo ---- Run %%~nG ----
  java -cp bin Main %%~fG
  echo.
)

for %%G in (samples\*.spg) do (
  echo ---- sPiglet %%~nG ----
  java -jar judge\spp.jar < %%~fG
  java -jar judge\pgi.jar < %%~fG > judge\tmp.txt
  javac -d judge\tmp %%~pG%%~nG.java
  java -cp judge\tmp %%~nG > judge\goal.txt
  fc judge\tmp.txt judge\goal.txt
  echo.
)

for %%G in (samples\*.kg) do (
  echo ---- Kanga %%~nG ----
  java -jar judge\kgi.jar < %%~fG > judge\tmp.txt
  javac -d judge\tmp %%~pG%%~nG.java
  java -cp judge\tmp %%~nG > judge\goal.txt
  fc judge\tmp.txt judge\goal.txt
  echo.
)

pause

cd judge
del /Q tmp.txt
del /Q goal.txt
del /S /Q tmp
rmdir /Q tmp