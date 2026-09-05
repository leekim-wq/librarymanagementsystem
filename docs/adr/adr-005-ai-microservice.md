📁 ADR-005: Use Python Flask for AI Recommendations
Status: Accepted

Context:
The system needed an AI-powered book recommendation feature. The main application is built in Java, but AI/ML libraries are more mature in Python.

Decision:
We developed a lightweight Python Flask microservice that handles book recommendations using content-based filtering with TF-IDF and cosine similarity.

Rationale:

Python offers superior ML libraries (scikit-learn, pandas) for recommendation algorithms.

Flask is lightweight and easy to deploy.

The service is stateless and communicates via REST API.

The microservice approach decouples AI logic from the main application.

Easy to replace or upgrade the recommendation algorithm independently.

Consequences:

The system depends on an external service running on port 5001.

The AI service must be started separately from the main application.

Network latency may affect response time (caching can mitigate this).

Alternatives Considered:

Java ML libraries (Weka, Deeplearning4j) – less mature and harder to implement.

OpenAI API – paid service, introduces external dependency.

Rule-based recommendations – less intelligent and not adaptive.