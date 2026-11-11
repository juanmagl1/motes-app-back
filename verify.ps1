$ErrorActionPreference = "Stop"

function Test-Endpoint {
    param($Method, $Url, $Body, $AuthToken)
    
    $Headers = @{ "Content-Type" = "application/json" }
    if ($AuthToken) { $Headers["Authorization"] = "Bearer $AuthToken" }
    
    try {
        if ($Body) {
            $Response = Invoke-RestMethod -Method $Method -Uri $Url -Headers $Headers -Body ($Body | ConvertTo-Json -Depth 10)
        } else {
            $Response = Invoke-RestMethod -Method $Method -Uri $Url -Headers $Headers
        }
        Write-Host "✅ $Method $Url - Success"
        return $Response
    } catch {
        Write-Host "❌ $Method $Url - Failed: $_"
        return $null
    }
}

Write-Host "Starting Verification..."

# 1. Login
$LoginBody = @{
    email = "demo@motes.com"
    password = "demo123"
}
$LoginResponse = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/auth/login" -Body $LoginBody

if (-not $LoginResponse) { exit 1 }
$Token = $LoginResponse.token
Write-Host "Got Token: $($Token.Substring(0, 20))..."

# 2. Get Personas
$Personas = Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/personas" -AuthToken $Token

# 3. Create Persona
$PersonaBody = @{
    nombre = "Test Persona $(Get-Random)"
    crearCuenta = $false
}
$NewPersona = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/personas" -Body $PersonaBody -AuthToken $Token

if ($NewPersona) {
    # 4. Create Mote
    $MoteBody = @{
        personaId = $NewPersona.id
        mote = "El Test $(Get-Random)"
        descripcion = "Test description"
    }
    $NewMote = Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/motes" -Body $MoteBody -AuthToken $Token
    
    if ($NewMote) {
        # 5. Toggle Vote
        $VoteBody = @{ moteId = $NewMote.id }
        Test-Endpoint -Method "POST" -Url "http://localhost:8080/api/votos/toggle" -Body $VoteBody -AuthToken $Token
        
        # 6. Get Top 5
        Test-Endpoint -Method "GET" -Url "http://localhost:8080/api/motes/top5" -AuthToken $Token
    }
}

Write-Host "Verification Completed!"
