provider "minio" {
  minio_server   = var.minio_server
  minio_user     = var.minio_user
  minio_password = var.minio_password
  minio_ssl      = var.minio_ssl
}

resource "minio_s3_bucket" "agentgo" {
  bucket = var.minio_bucket
  acl    = "private"
}
