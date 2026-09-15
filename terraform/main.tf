resource "aws_apigatewayv2_api" "api_manager" { //this creates the API GT itself
  name          = "api-mindicador"
  protocol_type = "HTTP"

  cors_configuration {
    allow_origins = ["http://localhost:5173", local.url_amplify,]
    allow_methods = ["GET", "OPTIONS", "POST", "PUT", "DELETE"]
    allow_headers = ["Authorization", "Content-Type"]
  }
}


resource "aws_apigatewayv2_stage" "default" {
  api_id      = aws_apigatewayv2_api.api_manager.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_apigatewayv2_stage" "dev" { //creates dev stage
  api_id      = aws_apigatewayv2_api.api_manager.id
  name        = "dev"
  auto_deploy = true
}

resource "aws_apigatewayv2_authorizer" "cognito" { //creates the JWT authorizer
  api_id           = aws_apigatewayv2_api.api_manager.id
  authorizer_type  = "JWT"
  identity_sources = ["$request.header.Authorization"]
  name             = "cognito-authorizer"

  jwt_configuration {
    audience = [aws_cognito_user_pool_client.spa.id]
    issuer   = "https://cognito-idp.us-east-1.amazonaws.com/${aws_cognito_user_pool.pool.id}"
  }
}

resource "aws_apigatewayv2_integration" "solicitudes_coleccion" {
  api_id                 = aws_apigatewayv2_api.api_manager.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "https://example.com/solicitudes" //placeholder only, doesn't matter because ECS deploy overwrites them 
  payload_format_version = "1.0"

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

resource "aws_apigatewayv2_integration" "solicitudes_decision" {
  api_id                 = aws_apigatewayv2_api.api_manager.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "https://example.com/solicitudes/decision"
  payload_format_version = "1.0"

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

resource "aws_apigatewayv2_integration" "solicitudes_elemento" {
  api_id                 = aws_apigatewayv2_api.api_manager.id
  integration_type       = "HTTP_PROXY"
  integration_method     = "ANY"
  integration_uri        = "https://example.com/solicitudes/solicitud"
  payload_format_version = "1.0"

  lifecycle {
    ignore_changes = [integration_uri]
  }
}

locals {
  rutas_solicitudes = {
    "GET /solicitudes"                = { scope = "solicitudes/read",    integracion = aws_apigatewayv2_integration.solicitudes_coleccion.id }
    "POST /solicitudes"               = { scope = "solicitudes/write",   integracion = aws_apigatewayv2_integration.solicitudes_coleccion.id }
    "GET /solicitudes/{id}"           = { scope = "solicitudes/read",    integracion = aws_apigatewayv2_integration.solicitudes_elemento.id }
    "PUT /solicitudes/{id}"           = { scope = "solicitudes/write",   integracion = aws_apigatewayv2_integration.solicitudes_elemento.id }
    "DELETE /solicitudes/{id}"        = { scope = "solicitudes/write",   integracion = aws_apigatewayv2_integration.solicitudes_elemento.id }
    "PUT /solicitudes/{id}/decision" = { scope = "solicitudes/aprobar", integracion = aws_apigatewayv2_integration.solicitudes_decision.id }
  }
}

resource "aws_apigatewayv2_route" "solicitudes" {
  for_each = local.rutas_solicitudes

  api_id    = aws_apigatewayv2_api.api_manager.id
  route_key = each.key
  target    = "integrations/${each.value.integracion}"

  authorization_type   = "JWT"
  authorizer_id         = aws_apigatewayv2_authorizer.cognito.id
  authorization_scopes = [each.value.scope]
}


//------------------------API GT OUTPUTS ------------------------

output "api_id" {
  value = aws_apigatewayv2_api.api_manager.id
}

output "integracion_solicitudes_coleccion_id" {
  value = aws_apigatewayv2_integration.solicitudes_coleccion.id 
}

output "integracion_solicitudes_elemento_id" {
  value = aws_apigatewayv2_integration.solicitudes_elemento.id
}

output "integracion_solicitudes_decision_id" {
  value = aws_apigatewayv2_integration.solicitudes_decision.id
}