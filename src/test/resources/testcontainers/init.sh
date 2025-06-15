## Quando viene aggiornato questo file, aggiornare anche il commitId presente nel file initsh-for-testcontainer-sh


echo " - Create pn-action-manager TABLES"


aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name Action \
    --attribute-definitions \
        AttributeName=actionId,AttributeType=S \
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
    --key-schema \
        AttributeName=timeSlot,KeyType=HASH \
        AttributeName=actionId,KeyType=RANGE \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5


echo "Initialization terminated"
