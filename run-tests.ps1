$ErrorActionPreference = 'Stop'
# Run the same compiled JUnit classes without Gradle's non-ASCII-path worker issue.
function Find-TestJar([string]$artifact) {
    $root = Join-Path $env:USERPROFILE ('.gradle\caches\modules-2\files-2.1\' + $artifact)
    $jar = Get-ChildItem -LiteralPath $root -Filter '*.jar' -Recurse | Select-Object -First 1 -ExpandProperty FullName
    if (-not $jar) { throw "Missing test dependency: $artifact. Run compileDebugUnitTestKotlin first." }
    return $jar
}
$testCp = @(
    (Join-Path $PSScriptRoot 'app\build\tmp\kotlin-classes\debug'),
    (Join-Path $PSScriptRoot 'app\build\tmp\kotlin-classes\debugUnitTest'),
    (Find-TestJar 'junit\junit\4.13.2'),
    (Find-TestJar 'org.hamcrest\hamcrest-core\1.3'),
    (Find-TestJar 'org.jetbrains.kotlin\kotlin-stdlib\2.0.20')
) -join ';'
& java '-Dfile.encoding=UTF-8' -cp $testCp org.junit.runner.JUnitCore io.github.sixzleo.tabfold.ConsentPolicyTest io.github.sixzleo.tabfold.device.CompatibilityTest io.github.sixzleo.tabfold.sensors.OpeningMathTest io.github.sixzleo.tabfold.auto.AutoPolicyTest io.github.sixzleo.tabfold.auto.ClosingDetectorTest
if ($LASTEXITCODE -ne 0) { throw 'Angle tests failed.' }
