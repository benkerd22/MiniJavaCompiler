@echo off
echo Start
cd ..
for %%G in (samples\*.java) do (
  echo ---- %%~nG ----
  java -cp bin Main %%~fG
  echo.
)

for %%G in (samples\*.spg) do (
  echo ---- %%~nG ----
  java -jar judge\pgi.jar < %%~fG > judge\tmp.txt
  javac -d judge\tmp %%~pG%%~nG.java
  java -cp judge\tmp %%~nG > judge\goal.txt
  fc judge\tmp.txt judge\goal.txt
  echo.
)

pause

del judge\tmp.txt
del judge\goal.txt
del /S /Q judge\tmp
rmdir judge\tmp