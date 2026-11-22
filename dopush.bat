@echo off
if exist .git\index.lock del /f /q .git\index.lock
"C:\Program Files\Git\cmd\git.exe" push origin main --force
"C:\Program Files\Git\cmd\git.exe" push origin v1.0.0 --force
