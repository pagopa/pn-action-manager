const isLambdaDisabled = (featureFlag, currentDate = new Date()) => {
  const start = new Date(featureFlag.start);
  const end = new Date(featureFlag.end);

  return currentDate < start || currentDate > end;
};

module.exports = {
  isLambdaDisabled
};