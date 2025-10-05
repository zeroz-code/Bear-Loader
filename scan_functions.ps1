# Scan Kotlin and Java files for function/method names and report duplicates
$root = Split-Path -Path $MyInvocation.MyCommand.Definition -Parent
Set-Location $root
$ktFiles = Get-ChildItem -Path .\app -Recurse -Include *.kt -File -ErrorAction SilentlyContinue
$javaFiles = Get-ChildItem -Path .\app -Recurse -Include *.java -File -ErrorAction SilentlyContinue
$ktMap = @{}
foreach ($f in $ktFiles) {
    $lines = Get-Content $f.FullName -ErrorAction SilentlyContinue
    for ($i=0;$i -lt $lines.Count;$i++){
        $ln = $lines[$i]
        if ($ln -match '^\s*fun\s+([A-Za-z0-9_]+)\s*\(') {
            $name = $Matches[1]
            if (-not $ktMap.ContainsKey($name)) { $ktMap[$name] = New-Object System.Collections.Generic.List[string] }
            if (-not $ktMap[$name].Contains($f.FullName)) { $ktMap[$name].Add($f.FullName) }
        }
    }
}
$javaMap = @{}
foreach ($f in $javaFiles) {
    $lines = Get-Content $f.FullName -ErrorAction SilentlyContinue
    for ($i=0;$i -lt $lines.Count;$i++){
        $ln = $lines[$i]
        if ($ln -match '^\s*(public|protected|private)\s+[A-Za-z0-9_<>\[\]]+\s+([A-Za-z0-9_]+)\s*\(') {
            $name = $Matches[2]
            if (-not $javaMap.ContainsKey($name)) { $javaMap[$name] = New-Object System.Collections.Generic.List[string] }
            if (-not $javaMap[$name].Contains($f.FullName)) { $javaMap[$name].Add($f.FullName) }
        }
    }
}
# Find common names
$common = @()
foreach ($k in $ktMap.Keys) {
    if ($javaMap.ContainsKey($k)) {
        $common += $k
    }
}
$outDir = Join-Path $root 'build'
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }
$outFile = Join-Path $outDir 'scan_results.csv'
"Function,KotlinFiles,JavaFiles,KotlinFileLines,JavaFileLines,Notes" | Out-File -FilePath $outFile -Encoding utf8
if ($common.Count -eq 0) {
    "No duplicate function names found between .kt and .java under app/" | Tee-Object -FilePath $outFile -Append
    Write-Host "No duplicates found. Output written to $outFile"
    exit 0
}
foreach ($name in $common | Sort-Object) {
    $kfiles = $ktMap[$name] -join ';'
    $jfiles = $javaMap[$name] -join ';'
    $kLinesMax = ($ktMap[$name] | ForEach-Object { (Get-Content $_).Count } | Measure-Object -Maximum).Maximum
    $jLinesMax = ($javaMap[$name] | ForEach-Object { (Get-Content $_).Count } | Measure-Object -Maximum).Maximum
    $notes = @()
    if ($kLinesMax -gt 300 -or $jLinesMax -gt 300) { $notes += 'Large file(s)' }
    if ([math]::Abs($kLinesMax - $jLinesMax) -gt ([math]::Max(50, [math]::Round([math]::Min($kLinesMax,$jLinesMax)*0.3)))) { $notes += 'Size mismatch' }
    $line = "$name,$( '"' + $kfiles + '"' ),$( '"' + $jfiles + '"' ),$kLinesMax,$jLinesMax,$( '"' + ($notes -join ';') + '"' )"
    $line | Out-File -FilePath $outFile -Append -Encoding utf8
}
Write-Host "Scan complete. Results written to $outFile"
