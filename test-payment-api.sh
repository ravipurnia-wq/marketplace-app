#!/bin/bash

# TechMarket Pro - Payment API Testing Script
echo "🛒 TechMarket Pro - Payment API Testing"
echo "========================================"

BASE_URL="http://localhost:8081"
API_URL="$BASE_URL/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_TOTAL=0

# Function to run test
run_test() {
    local test_name="$1"
    local expected_status="$2"
    local curl_command="$3"
    
    TESTS_TOTAL=$((TESTS_TOTAL + 1))
    echo -e "\n${YELLOW}Test $TESTS_TOTAL: $test_name${NC}"
    
    response=$(eval "$curl_command")
    actual_status=$(echo "$response" | jq -r '.status')
    
    if [ "$actual_status" = "$expected_status" ]; then
        echo -e "${GREEN}✅ PASSED${NC}"
        echo "Response: $response"
        TESTS_PASSED=$((TESTS_PASSED + 1))
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "Expected status: $expected_status"
        echo "Actual response: $response"
    fi
}

echo -e "\n📡 Testing server availability..."
if curl -s "$BASE_URL/" > /dev/null; then
    echo -e "${GREEN}✅ Server is running on $BASE_URL${NC}"
else
    echo -e "${RED}❌ Server is not running on $BASE_URL${NC}"
    echo "Please start the application first: mvn spring-boot:run"
    exit 1
fi

echo -e "\n🛍️ Getting product list..."
products=$(curl -s "$API_URL/products")
product_count=$(echo "$products" | jq length)
echo "Found $product_count products"

if [ "$product_count" -gt 0 ]; then
    first_product=$(echo "$products" | jq '.[0]')
    product_id=$(echo "$first_product" | jq -r '.id')
    paypal_id=$(echo "$first_product" | jq -r '.paypalButtonId')
    echo "Using Product ID: $product_id, PayPal ID: $paypal_id"
else
    echo -e "${RED}❌ No products found${NC}"
    exit 1
fi

# Test 1: Valid payment request
run_test "Valid Payment Request" "success" \
    "curl -s -X POST '$API_URL/payments/paypal' -H 'Content-Type: application/json' -d '{\"productId\": $product_id, \"paypalButtonId\": \"$paypal_id\"}' | jq ."

# Test 2: Invalid product ID
run_test "Invalid Product ID" "error" \
    "curl -s -X POST '$API_URL/payments/paypal' -H 'Content-Type: application/json' -d '{\"productId\": \"invalid\", \"paypalButtonId\": \"$paypal_id\"}' | jq ."

# Test 3: Empty PayPal button ID
run_test "Empty PayPal Button ID" "error" \
    "curl -s -X POST '$API_URL/payments/paypal' -H 'Content-Type: application/json' -d '{\"productId\": $product_id, \"paypalButtonId\": \"\"}' | jq ."

# Test 4: Missing required fields
run_test "Missing Required Fields" "error" \
    "curl -s -X POST '$API_URL/payments/paypal' -H 'Content-Type: application/json' -d '{}' | jq ."

# Test 5: Payment success callback
run_test "Payment Success Callback" "completed" \
    "curl -s -X POST '$API_URL/payments/paypal/success' -H 'Content-Type: application/json' -d '{\"productId\": $product_id, \"transactionId\": \"TXN123\"}' | jq ."

# Test 6: Payment cancel callback
run_test "Payment Cancel Callback" "cancelled" \
    "curl -s -X POST '$API_URL/payments/paypal/cancel' -H 'Content-Type: application/json' -d '{\"productId\": $product_id, \"reason\": \"user_cancelled\"}' | jq ."

# Test 7: Test with different product IDs
if [ "$product_count" -gt 1 ]; then
    second_product_id=$(echo "$products" | jq -r '.[1].id')
    run_test "Second Product Payment" "success" \
        "curl -s -X POST '$API_URL/payments/paypal' -H 'Content-Type: application/json' -d '{\"productId\": $second_product_id, \"paypalButtonId\": \"$paypal_id\"}' | jq ."
fi

# Test 8: Verify PayPal URL format
echo -e "\n${YELLOW}Test: PayPal URL Format Validation${NC}"
payment_response=$(curl -s -X POST "$API_URL/payments/paypal" -H 'Content-Type: application/json' -d "{\"productId\": $product_id, \"paypalButtonId\": \"$paypal_id\"}")
paypal_url=$(echo "$payment_response" | jq -r '.paypalUrl')

if [[ "$paypal_url" == *"paypal.com"* ]] && [[ "$paypal_url" == *"$paypal_id"* ]]; then
    echo -e "${GREEN}✅ PASSED${NC}"
    echo "PayPal URL is correctly formatted: $paypal_url"
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}❌ FAILED${NC}"
    echo "Invalid PayPal URL: $paypal_url"
fi
TESTS_TOTAL=$((TESTS_TOTAL + 1))

# Summary
echo -e "\n📊 Test Summary"
echo "==============="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Total:  $TESTS_TOTAL"

if [ "$TESTS_PASSED" -eq "$TESTS_TOTAL" ]; then
    echo -e "\n🎉 ${GREEN}All tests passed! Payment API is working correctly.${NC}"
    exit 0
else
    failed=$((TESTS_TOTAL - TESTS_PASSED))
    echo -e "\n⚠️  ${RED}$failed test(s) failed. Please check the issues above.${NC}"
    exit 1
fi