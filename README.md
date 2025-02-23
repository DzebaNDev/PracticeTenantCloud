# Real Estate Listing Generator Lambda Function

## General Settings for AWS Lambda Setup

```
Timeout = 0min 30sec
Memory (MB) = 128
```

## Requirements
- Java 23
- Maven

## Description
This AWS Lambda function processes real estate listings in JSON format and generates detailed descriptions using OpenAI's GPT model.

## Input
- JSON containing listing details such as title, address, price, amenities, etc.

## Output
- A generated text description based on the listing details.

## Local Setup

### Setup environment variables
```bash
export OPENAI_API_KEY="your_openai_api_key"
```

### Build and Package the Project
```bash
mvn clean package
```

### Deploy to AWS Lambda
Use AWS CLI or AWS Management Console to deploy the generated JAR file.


### Example request
```json
{
  "body": "{ \"items\": [ { \"listingId\": 6119234, \"title\": \"2 Bedroom Apartment in Ogden\", \"description\": \"Простора квартира з двома спальнями та сучасним ремонтом. Велика кухня, нова техніка та балкон з видом на місто.\", \"address1\": \"3042 Washington Blvd\", \"city\": \"Ogden\", \"state\": \"UT\", \"zip\": \"84401\", \"minPrice\": 1390, \"minSquareFeet\": 834, \"minBedrooms\": 2, \"minBathrooms\": 1, \"communityTitle\": \"The Carlo at Washington\", \"amenities\": [\"dishwasher\", \"hardwood floors\", \"air conditioning\"], \"customAmenities\": [\"walk-in closet\", \"ceiling fan\"], \"communityCustomAmenities\": [\"fitness center\", \"swimming pool\"] } ] }"
}
```

### Example response
```json
{
  "statusCode": 200,
  "body": "[{\"listingId\":6119234,\"generatedDescription\":\"Розташування цієї нерухомості є дуже зручним, оскільки вона знаходиться на вулиці Washington Blvd в місті Ogden, штат Юта. Квартира має простору кухню з новою технікою, а також балкон з чудовим видом на місто. У квартирі є дві спальні, одна ванна кімната, посудомийна машина, паркетні підлоги та кондиціонер.\\n\\nКомплекс The Carlo at Washington, де розташована ця квартира, має власний фітнес-центр та басейн для мешканців. Додаткові зручності квартири включають гардеробну кімнату та вентилятор на стелі.\\n\\nЦіна цієї квартири становить $1390 за місяць, що робить її привабливим варіантом для тих, хто шукає комфортне житло в цьому районі. Площа квартири становить 834 квадратних футів, що також робить її достатньо\"}]"
}
```


