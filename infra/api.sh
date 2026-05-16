#!/bin/sh

host="172.22.80.1"

# User register
username="myUser"
curl -s -X POST http://$host:8080/api/auth/register -H "Content-Type: application/json" -d "{ \"username\": \"$username\", \"password\": "123456" }"

# User login
user_token=$(curl -s -X POST http://$host:8080/api/auth/login -H "Content-Type: application/json" -d '{ "username": "user", "password": "user123" }')
echo $user_token | jq '.'
user_token=$(echo $user_token | jq -r '.jwt')
echo "User token: $user_token"

# Admin login
admin_token=$(curl -s -X POST http://$host:8080/api/auth/login -H "Content-Type: application/json" -d '{ "username": "admin", "password": "admin123" }')
echo $admin_token | jq '.'
admin_token=$(echo $admin_token | jq -r '.jwt')
echo "Admin token: $admin_token"

# Draw create
draw=$(curl -s -X POST http://$host:8080/api/draws \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $admin_token" \
-d '
{
  "title": "New draw",
  "numbersCount": 5,
  "maxNumber": 30
}')
echo $draw | jq '.'
draw_id=$(echo $draw | jq -r '.id')
echo "Id of created draw: $draw_id"

# Draw get active
draw=$(curl -s -X GET http://$host:8080/api/draws/active \
-H "Authorization: Bearer $admin_token")
echo $draw | jq '.'

# Ticket buy
ticket=$(curl -s -X POST http://$host:8080/api/tickets \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $user_token" \
-d "
{
  \"drawId\": $draw_id,
  \"numbers\": [1, 7, 17, 23, 25]
}")
echo $ticket | jq '.'
ticket_id=$(echo $ticket | jq '.id')
echo "Id of bought ticket: $ticket_id"

# Ticket get
ticket=$(curl -s -X GET http://$host:8080/api/tickets/$ticket_id/result \
-H "Authorization: Bearer $user_token")
echo $ticket | jq '.'

# Draw complete
draw=$(curl -s -X POST http://$host:8080/api/draws/$draw_id/complete \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $admin_token")
echo $draw | jq '.'

# Draw get result
draw=$(curl -s -X GET http://$host:8080/api/draws/$draw_id/result \
-H "Authorization: Bearer $admin_token")
echo $draw | jq '.'

# Draw get tickets
draw=$(curl -s -X GET http://$host:8080/api/draws/$draw_id/tickets?status=LOSE \
-H "Authorization: Bearer $admin_token")
echo $draw | jq '.'