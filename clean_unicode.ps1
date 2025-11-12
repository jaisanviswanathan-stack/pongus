$filePath = "c:\Users\jaisa\OneDrive\Documents\Coding Projects\Java Projects\pong_with_learning_ai\PingPongGame.java"
$content = Get-Content $filePath -Raw -Encoding UTF8

# Remove corrupted Unicode patterns - replace with simple ASCII
$content = $content -creplace 'ÃƒÆ.{1,100}?Â¢', '- '
$content = $content -creplace 'ÃƒÆ.{1,50}?(v|Down)', 'v'
$content = $content -creplace 'Ã‚Â°', ''
$content = $content -creplace 'Ãƒ[^a-zA-Z0-9 ]{1,100}', ''
$content = $content -creplace 'Â[^a-zA-Z0-9 ]', ''

Set-Content $filePath -Value $content -Encoding UTF8 -NoNewline
Write-Host "Cleanup complete!"
