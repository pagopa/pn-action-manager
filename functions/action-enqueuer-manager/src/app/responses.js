function generateKoResponse(err) {
  console.debug("GenerateKoResponse this err ", err);

  const responseBody = {};

  let statusCode = 500;
  responseBody.error = err;

  responseBody.status = statusCode;

  return {
    statusCode: statusCode,
    body: JSON.stringify(responseBody),
    isBase64Encoded: false,
  };
}
module.exports = { generateKoResponse };
