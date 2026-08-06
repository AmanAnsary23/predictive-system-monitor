import mysql.connector
import pandas as pd
from sklearn.ensemble import IsolationForest
from flask import Flask, request, jsonify

app = Flask(__name__)

# Train the model once when the server starts
def train_model():
    conn = mysql.connector.connect(
        host="localhost",
        port=3306,
        user="root",
        password="root",
        database="system_predector"
    )
    query = "SELECT cpu_usage, db_connections, response_time_ms FROM system_metrics"
    df = pd.read_sql(query, conn)
    conn.close()

    model = IsolationForest(contamination=0.1, random_state=42)
    model.fit(df)
    return model

model = train_model()
print("Model trained and ready!")

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()
    cpu = data["cpuUsage"]
    db_conn = data["dbConnections"]
    response_time = data["responseTimeMs"]

    input_df = pd.DataFrame([[cpu, db_conn, response_time]],
                             columns=["cpu_usage", "db_connections", "response_time_ms"])

    prediction = model.predict(input_df)[0]  # 1 = normal, -1 = anomaly
    is_anomaly = bool(prediction == -1)

    return jsonify({"isAnomaly": is_anomaly})

if __name__ == "__main__":
    app.run(port=5000)