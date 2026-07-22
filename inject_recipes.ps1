[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName "System.IO.Compression"
Add-Type -AssemblyName "System.IO.Compression.FileSystem"

$jarFiles = Get-ChildItem "D:\Plain Craft Launcher 2\.minecraft\versions" -Recurse -Filter "*sophisticatedbackpacks-1.20.1-3.24.60.1982.jar"

if ($null -eq $jarFiles -or $jarFiles.Count -eq 0) {
    Write-Host "Jar files not found!"
    exit 1
}

$r1 = '{"type":"minecraft:crafting_shaped","category":"misc","pattern":["DGD","GBG","DGD"],"key":{"D":{"item":"minecraft:diamond"},"G":{"item":"minecraft:gold_ingot"},"B":{"item":"sophisticatedbackpacks:upgrade_base"}},"result":{"item":"sophisticatedbackpacks:survival_infinibag_upgrade"}}'

$r2 = '{"type":"minecraft:crafting_shaped","category":"misc","pattern":["EDE","DBD","EDE"],"key":{"E":{"item":"minecraft:emerald"},"D":{"item":"minecraft:diamond"},"B":{"item":"sophisticatedbackpacks:upgrade_base"}},"result":{"item":"sophisticatedbackpacks:infinibag_upgrade"}}'

$r3 = '{"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"sophisticatedbackpacks:infinibag_upgrade"}],"result":{"item":"sophisticatedbackpacks:survival_infinibag_upgrade"}}'

$r4 = '{"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"sophisticatedbackpacks:survival_infinibag_upgrade"}],"result":{"item":"sophisticatedbackpacks:infinibag_upgrade"}}'

function AddFile($zipObj, $path, $content) {
    $e = $zipObj.GetEntry($path)
    if ($null -ne $e) { $e.Delete() }
    $ne = $zipObj.CreateEntry($path)
    $writer = New-Object System.IO.StreamWriter($ne.Open())
    $writer.Write($content)
    $writer.Close()
    Write-Host "Successfully injected: $path"
}

foreach ($jar in $jarFiles) {
    Write-Host "Target Jar Path: " $jar.FullName
    $zip = [System.IO.Compression.ZipFile]::Open($jar.FullName, [System.IO.Compression.ZipArchiveMode]::Update)
    AddFile $zip "data/sophisticatedbackpacks/recipes/survival_infinibag_upgrade.json" $r1
    AddFile $zip "data/sophisticatedbackpacks/recipes/infinibag_upgrade.json" $r2
    AddFile $zip "data/sophisticatedbackpacks/recipes/infinibag_to_survival.json" $r3
    AddFile $zip "data/sophisticatedbackpacks/recipes/survival_to_infinibag.json" $r4
    $zip.Dispose()
}

Write-Host "DONE! All 4 JSON recipes injected into all Sophisticated Backpacks jars across version folders!"
