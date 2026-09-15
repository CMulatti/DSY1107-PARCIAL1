/**
 * user-token-ms -- el puente entre "a que grupo pertenece el usuario" y
 * "que dice el claim scope de su access token", mas el correo del usuario
 * para que el backend pueda saber de quien es cada solicitud.
 *
 * Cognito lo invoca en el trigger Pre Token Generation V2: despues de haber
 * autenticado al usuario y justo antes de firmar los tokens.
 */

const SCOPES_POR_GRUPO = {
  solicitantes: ['solicitudes/read', 'solicitudes/write'],
  aprobadores: ['solicitudes/read', 'solicitudes/aprobar'],
};

export const handler = async (event) => {
  const grupos = event.request.groupConfiguration?.groupsToOverride ?? [];
  const scopes = [...new Set(grupos.flatMap((grupo) => SCOPES_POR_GRUPO[grupo] ?? []))];
  const email = event.request.userAttributes?.email ?? null;

  console.log(
    JSON.stringify({
      usuario: email ?? event.userName,
      grupos,
      scopes,
    }),
  );

  event.response = {
    claimsAndScopeOverrideDetails: {
      accessTokenGeneration: {
        scopesToAdd: scopes,
        // El access token normalmente no trae email (eso vive en el ID
        // token). Se agrega aqui explicitamente porque el backend solo ve
        // el access token via Authorization: Bearer, y necesita saber de
        // quien es cada solicitud sin volver a llamar a Cognito.
        claimsToAddOrOverride: {
          email: email,
        },
      },
    },
  };

  return event;
};