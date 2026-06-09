$ErrorActionPreference = "Stop"

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$LocalDir = Join-Path $Root ".local"
$LogDir = Join-Path $LocalDir "logs"
$EnvPath = Join-Path $Root ".env"
$MysqlBase = "C:\Program Files\MySQL\MySQL Server 8.0"
$MysqlHost = "127.0.0.1"
$MysqlPort = 3306
$BackendPort = 8080
$ChromaPort = 8000

New-Item -ItemType Directory -Force -Path $LocalDir, $LogDir | Out-Null

if (Test-Path $EnvPath) {
    Get-Content $EnvPath | ForEach-Object {
        $Line = $_.Trim()
        if ($Line -and -not $Line.StartsWith("#") -and $Line.Contains("=")) {
            $Key, $Value = $Line.Split("=", 2)
            Set-Item -Path "Env:$Key" -Value $Value
        }
    }
}

function Wait-Http($Url, $Name) {
    for ($i = 0; $i -lt 60; $i++) {
        try {
            Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "$Name startup timed out: $Url"
}

function Wait-Mysql($MysqlAdmin, $Password) {
    $OldMysqlPwd = $env:MYSQL_PWD
    $env:MYSQL_PWD = $Password
    for ($i = 0; $i -lt 60; $i++) {
        & $MysqlAdmin "--host=$MysqlHost" "--port=$MysqlPort" "--user=root" ping --silent 2>$null
        if ($LASTEXITCODE -eq 0) {
            $env:MYSQL_PWD = $OldMysqlPwd
            return
        }
        Start-Sleep -Seconds 1
    }
    $env:MYSQL_PWD = $OldMysqlPwd
    throw "MySQL startup timed out: $MysqlHost`:$MysqlPort"
}

function Initialize-Mysql {
    $MysqlBin = Join-Path $MysqlBase "bin"
    $Mysql = Join-Path $MysqlBin "mysql.exe"
    $MysqlAdmin = Join-Path $MysqlBin "mysqladmin.exe"
    if (-not (Test-Path $Mysql)) {
        throw "MySQL client not found: $Mysql"
    }
    if (-not $env:MYSQL_PASSWORD) {
        throw ".env is missing MYSQL_PASSWORD"
    }

    Wait-Mysql $MysqlAdmin $env:MYSQL_PASSWORD
    $OldMysqlPwd = $env:MYSQL_PWD
    $env:MYSQL_PWD = $env:MYSQL_PASSWORD
    & $Mysql "--host=$MysqlHost" "--port=$MysqlPort" "--user=root" -e "CREATE DATABASE IF NOT EXISTS smart_health CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>$null
    $env:MYSQL_PWD = $OldMysqlPwd
}

function Start-Chroma {
    $Existing = Get-NetTCPConnection -LocalPort $ChromaPort -ErrorAction SilentlyContinue
    if ($Existing) {
        return
    }

    $VenvDir = Join-Path $LocalDir "chroma-venv"
    $ChromaData = Join-Path $LocalDir "chroma-data"
    $ChromaExe = Join-Path $VenvDir "Scripts\chroma.exe"

    if (-not (Test-Path $ChromaExe)) {
        python -m venv $VenvDir
        & (Join-Path $VenvDir "Scripts\python.exe") -m pip install --upgrade pip
        & (Join-Path $VenvDir "Scripts\python.exe") -m pip install "chromadb==0.5.18"
    }

    New-Item -ItemType Directory -Force -Path $ChromaData | Out-Null
    Start-Process -FilePath $ChromaExe `
        -ArgumentList @("run", "--host", "127.0.0.1", "--port", "$ChromaPort", "--path", "`"$ChromaData`"") `
        -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $LogDir "chroma.out.log") `
        -RedirectStandardError (Join-Path $LogDir "chroma.err.log")

    Wait-Http "http://127.0.0.1:$ChromaPort/api/v1/heartbeat" "Chroma"
}

Initialize-Mysql
Start-Chroma

$env:JAVA_HOME = (Resolve-Path (Join-Path $Root "tools\jdk17\jdk-17.0.19+10")).Path
$env:Path = "$env:JAVA_HOME\bin;$(Resolve-Path (Join-Path $Root "tools\apache-maven-3.9.9\bin"));$env:Path"
$env:MYSQL_USERNAME = "root"
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "SmartHealth2026SecretKeyForJWTTokenGenerationMustBe256BitsLong!!"
}
if (-not $env:DEEPSEEK_API_KEY) {
    $env:DEEPSEEK_API_KEY = ""
}
$env:CHROMA_URL = "http://127.0.0.1:$ChromaPort"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://$MysqlHost`:$MysqlPort/smart_health?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"

$BackendExisting = Get-NetTCPConnection -LocalPort $BackendPort -ErrorAction SilentlyContinue
if ($BackendExisting) {
    Write-Host "Backend port $BackendPort is already in use, skipping startup."
} else {
    $RunBackend = Join-Path $PSScriptRoot "run-backend.ps1"
    Start-Process -FilePath "powershell" `
        -ArgumentList @("-ExecutionPolicy", "Bypass", "-File", "`"$RunBackend`"") `
        -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $LogDir "backend.out.log") `
        -RedirectStandardError (Join-Path $LogDir "backend.err.log")
}

Write-Host "MySQL: $MysqlHost`:$MysqlPort"
Write-Host "Chroma: http://127.0.0.1:$ChromaPort"
Write-Host "Backend: http://127.0.0.1:$BackendPort"
