Write-Host "Updating Lambda function..."

$LAMBDA_FUNCTION_NAME = "listing-description-generator"
$ZIP_FILE = "Practice_TenantCloud-1.0-SNAPSHOT.jar"

aws lambda update-function-code --function-name $LAMBDA_FUNCTION_NAME --zip-file "fileb://$ZIP_FILE"

Write-Host "Update completed!"
