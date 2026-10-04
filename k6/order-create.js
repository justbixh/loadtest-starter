import http from 'k6/http';
import { check } from 'k6';

const csvRows = open('../gatling/src/test/resources/orders.csv')
  .trim()
  .split('\n')
  .slice(1)
  .map((line) => {
    const [sku, quantity, unitPrice, customerType] = line.split(',');
    return { sku, quantity: Number(quantity), unitPrice: Number(unitPrice), customerType };
  });

const baseUrl = __ENV.MOCK_API_URL || 'http://127.0.0.1:3000';
const rate = Number(__ENV.RATE || 10);
const duration = __ENV.DURATION || '30s';

export const options = {
  scenarios: {
    order_create: {
      executor: 'constant-arrival-rate',
      rate,
      timeUnit: '1s',
      duration,
      preAllocatedVUs: 20,
      maxVUs: 100,
    },
  },
};

export default function () {
  const order = csvRows[__ITER % csvRows.length];
  const response = http.post(`${baseUrl}/api/orders`, JSON.stringify(order), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /api/orders' },
  });

  check(response, {
    'status is 201': (res) => res.status === 201,
    'order is accepted': (res) => res.json('status') === 'accepted',
    'response includes order ID': (res) => Boolean(res.json('orderId')),
  });
}
