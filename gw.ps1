$jbr = "C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
if (-not (Test-Path $jbr)) {
    $jbr = "java"
}
& $jbr -jar ".\gradle\wrapper\gradle-wrapper.jar" @args
