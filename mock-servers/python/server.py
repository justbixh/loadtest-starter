from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import json


class MockHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != "/health":
            self.send_error(404)
            return
        self._send_json(200, {"status": "ok"})

    def do_POST(self):
        if self.path != "/api/orders":
            self.send_error(404)
            return
        self.rfile.read(int(self.headers.get("Content-Length", "0")))
        self._send_json(
            201,
            {
                "status": "accepted",
                "message": "Order accepted",
                "orderId": "mock-order-001",
            },
        )

    def _send_json(self, status, payload):
        print(f"{self.command} {self.path} -> {status}", flush=True)
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, _format, *_args):
        pass


if __name__ == "__main__":
    print("Mock orders API listening on http://127.0.0.1:3000")
    ThreadingHTTPServer(("127.0.0.1", 3000), MockHandler).serve_forever()
