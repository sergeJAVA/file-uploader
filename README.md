# File-uploader service

## Description:

File-upload-service performs the first stage of validation the uploaded .xls or .xlsx file,
during which the file type and data size are verified. If the check is successful,
the service notifies another service, file-status-processor, of the successful completion, and the file is sent to the file-processor-service for the second stage of validation.

---

**Before you start the service you need to make sure that all data servers are up and running.**

---

**The docker-compose.yml file for running data servers is located in the file-uploader service. It must be launched before all services are started.**

---


**The application port is 8443**

---

# API Endpoint
**The service provides a single endpoint for uploading files. All requests must be made using the HTTPS protocol.**

- Method: **POST**

- URL: https://localhost:8443/upload

**Request:** The request must be of type multipart/form-data and include the file under the key **`file`**.
