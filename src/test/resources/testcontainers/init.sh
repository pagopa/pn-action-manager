#!/bin/bash
echo " - Create pn-action-manager TABLES"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name Action \
    --attribute-definitions \
        AttributeName=actionId,AttributeType=S \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_IMAGE \
    --key-schema \
        AttributeName=actionId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name FutureAction \
    --attribute-definitions \
        AttributeName=timeSlot,AttributeType=S \
        AttributeName=actionId,AttributeType=S \
    --stream-specification StreamEnabled=true,StreamViewType=NEW_AND_OLD_IMAGES \
    --key-schema \
        AttributeName=timeSlot,KeyType=HASH \
        AttributeName=actionId,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name LastPollForFutureAction \
    --attribute-definitions \
        AttributeName=lastPoolKey,AttributeType=N \
    --key-schema \
        AttributeName=lastPoolKey,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

# Inseriamo un record necessario per la lambda future-action-remover
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb put-item \
    --table-name LastPollForFutureAction \
    --item '{"lastPoolKey": {"N": "3"}, "lastPollExecuted": {"S": "2025-06-11T16:02"}}' \

echo " Create kinesis stream for Action and FutureAction tables"

aws kinesis create-stream \
    --stream-name action-stream \
    --shard-count 1 \
    --region us-east-1 \
    --endpoint-url=http://localstack:4566

aws kinesis create-stream \
    --stream-name future-action-stream \
    --shard-count 1 \
    --region us-east-1 \
    --endpoint-url=http://localstack:4566

echo " Bind table streams to kinesis streams"
aws dynamodb enable-kinesis-streaming-destination \
  --table-name Action \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/action-stream

# Controlla se la destinazione di streaming Kinesis è abilitata per la tabella Action
aws dynamodb describe-kinesis-streaming-destination \
  --table-name Action \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws dynamodb enable-kinesis-streaming-destination \
  --table-name FutureAction \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --stream-arn arn:aws:kinesis:us-east-1:000000000000:stream/future-action-stream

# Controlla se la destinazione di streaming Kinesis è abilitata per la tabella Action
aws dynamodb describe-kinesis-streaming-destination \
  --table-name FutureAction \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

# Creazione code Delivery push actions (NON DOVREBBE STARE QUI)
echo "### CREATE QUEUES ###"

queues="pn-delivery_push_actions pn-delivery_push_validation_actions pn-action_manager_action-timeout-error-DLQ"

for qn in  $( echo $queues | tr " " "\n" ) ; do
    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn
done


echo " Create pn-action-router lambda"

# Trasforma il tuo JSON originale convertendo gli oggetti in stringhe
# Estrai le variabili d'ambiente in formato KEY=VALUE
ENV_VARS=$(jq -r '.Variables | to_entries | map("\(.key)=\(.value|tostring)") | join(",")' /opt/lambdas/ActionRouterDefault.json)

aws lambda create-function \
  --function-name action-router-lambda \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --environment Variables="{$ENV_VARS}" \
  --timeout 30 \
  --region us-east-1 \
  --endpoint-url=http://localhost:4566 \
  --zip-file fileb:///opt/lambdas/action-router/function.zip

aws logs create-log-group \
  --log-group-name /aws/lambda/action-router-lambda \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws lambda create-event-source-mapping \
  --function-name action-router-lambda \
  --event-source-arn arn:aws:kinesis:us-east-1:000000000000:stream/action-stream \
  --starting-position LATEST \
  --filter-criteria '{"Filters":[{"Pattern":"{\"data\":{\"eventName\":[\"INSERT\"]}}"}]}' \
  --batch-size 100 \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --maximum-retry-attempts 2 \

echo " - Create future-action-remover lambda"

# Estrai le variabili d'ambiente in formato KEY=VALUE
ENV_VARS=$(jq -r '.Variables | to_entries | map("\(.key)=\(.value|tostring)") | join(",")' /opt/lambdas/ActionRemoverDefault.json)

aws lambda create-function \
  --function-name future-action-remover \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --timeout 30 \
  --environment Variables="{$ENV_VARS}" \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --zip-file fileb:///opt/lambdas/action-remover/function.zip

# Crea il log group per future-action-remover
aws logs create-log-group \
  --log-group-name /aws/lambda/future-action-remover \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

echo " - Create EventBridge scheduled rule for future-action-remover"
aws events put-rule \
  --name future-action-remover-schedule \
  --schedule-expression "rate(1 minute)" \
  --description "Scheduled rule to invoke future-action-remover Lambda every hour" \
  --state ENABLED \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws events put-targets \
  --rule future-action-remover-schedule \
  --targets '[
    {
      "Id": "1",
      "Arn": "arn:aws:lambda:us-east-1:000000000000:function:future-action-remover",
      "Input": "{\"source\":\"eventbridge-scheduler\",\"detail-type\":\"Scheduled Event\",\"detail\":{\"action\":\"cleanup\",\"timestamp\":\"scheduled\"}}"
    }
  ]' \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

# Concede permesso a EventBridge di invocare la Lambda
aws lambda add-permission \
  --function-name future-action-remover \
  --statement-id allow-eventbridge \
  --action lambda:InvokeFunction \
  --principal events.amazonaws.com \
  --source-arn arn:aws:events:us-east-1:000000000000:rule/future-action-remover-schedule \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

echo " Create pn-action-enqueuer lambda"

# Trasforma il tuo JSON originale convertendo gli oggetti in stringhe
# Estrai le variabili d'ambiente in formato KEY=VALUE
ENV_VARS=$(jq -r '.Variables | to_entries | map("\(.key)=\(.value|tostring)") | join(",")' /opt/lambdas/ActionEnqueuerDefault.json)

aws lambda create-function \
  --function-name action-enqueuer-lambda \
  --runtime nodejs18.x \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --handler index.handler \
  --environment Variables="{$ENV_VARS}" \
  --timeout 30 \
  --region us-east-1 \
  --endpoint-url=http://localhost:4566 \
  --zip-file fileb:///opt/lambdas/action-enqueuer/function.zip

aws logs create-log-group \
  --log-group-name /aws/lambda/action-enqueuer-lambda \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566

aws lambda create-event-source-mapping \
  --function-name action-enqueuer-lambda \
  --event-source-arn arn:aws:kinesis:us-east-1:000000000000:stream/future-action-stream \
  --starting-position LATEST \
  --filter-criteria '{"Filters":[{"Pattern":"{\"data\":{\"eventName\":[\"REMOVE\"]}}"}]}' \
  --batch-size 100 \
  --region us-east-1 \
  --endpoint-url=http://localstack:4566 \
  --maximum-retry-attempts 2 \

echo "Initialization terminated"