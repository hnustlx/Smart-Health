$ErrorActionPreference = "Stop"

$Root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$EnvPath = Join-Path $Root ".env"

if (Test-Path $EnvPath) {
    Get-Content $EnvPath | ForEach-Object {
        $Line = $_.Trim()
        if ($Line -and -not $Line.StartsWith("#") -and $Line.Contains("=")) {
            $Key, $Value = $Line.Split("=", 2)
            Set-Item -Path "Env:$Key" -Value $Value
        }
    }
}

$env:JAVA_HOME = (Resolve-Path (Join-Path $Root "tools\jdk17\jdk-17.0.19+10")).Path
$env:Path = "$env:JAVA_HOME\bin;$(Resolve-Path (Join-Path $Root "tools\apache-maven-3.9.9\bin"));$env:Path"
$env:MYSQL_USERNAME = "root"
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "SmartHealth2026SecretKeyForJWTTokenGenerationMustBe256BitsLong!!"
}
if (-not $env:DEEPSEEK_API_KEY) {
    $env:DEEPSEEK_API_KEY = ""
}
$env:CHROMA_URL = "http://127.0.0.1:8000"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/smart_health?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"

Set-Location (Join-Path $Root "smart-health-backend")
mvn spring-boot:run
