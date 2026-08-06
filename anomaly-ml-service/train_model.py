import mysql.connector
import pandas as pd
from sklearn.ensemble import IsolationForest

# Connect to your MySQL database
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

print("Total rows fetched:", len(df))

model = IsolationForest(contamination=0.1, random_state=42)
model.fit(df)

df["anomaly"] = model.predict(df)

print(df["anomaly"].value_counts())
print("\nSample anomalies detected:")
print(df[df["anomaly"] == -1].head())