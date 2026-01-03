#!/bin/bash
# =====================================================
# Delivery Service Database Initialization Script
# =====================================================
# 이 스크립트는 데이터베이스를 초기화하고 더미 데이터를 생성합니다.
#
# 사용법:
#   ./init_data.sh           # 스키마 + 더미 데이터 생성
#   ./init_data.sh --reset   # 기존 데이터 삭제 후 더미 데이터 생성
#   ./init_data.sh --schema  # 스키마만 생성
#   ./init_data.sh --data    # 더미 데이터만 생성
# =====================================================

set -e

# 데이터베이스 연결 정보
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-delivery}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-secret}"

# 스크립트 디렉토리
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 로그 출력
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 함수: PostgreSQL 명령 실행
run_psql() {
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME "$@"
}

# 함수: SQL 파일 실행
run_sql_file() {
    local file=$1
    log_info "Executing: $file"
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f "$file"
}

# 함수: 연결 테스트
test_connection() {
    log_info "Testing database connection..."
    if run_psql -c "SELECT 1;" > /dev/null 2>&1; then
        log_info "Database connection successful!"
        return 0
    else
        log_error "Failed to connect to database!"
        log_error "Please check if PostgreSQL is running and connection details are correct."
        exit 1
    fi
}

# 함수: 스키마 생성
create_schema() {
    log_info "Creating database schema..."
    run_sql_file "${SCRIPT_DIR}/schema.sql"
    log_info "Schema created successfully!"
}

# 함수: 데이터 삭제
reset_data() {
    log_warn "Resetting all data..."
    run_sql_file "${SCRIPT_DIR}/reset_data.sql"
    log_info "Data reset completed!"
}

# 함수: 더미 데이터 생성
generate_data() {
    log_info "Generating dummy data (10,000 products)..."
    log_info "This may take a few minutes..."
    run_sql_file "${SCRIPT_DIR}/dummy_data.sql"
    log_info "Dummy data generated successfully!"
}

# 함수: 통계 출력
show_stats() {
    log_info "Database statistics:"
    run_psql -c "
        SELECT
            schemaname as schema,
            relname as table_name,
            n_live_tup as row_count
        FROM pg_stat_user_tables
        WHERE schemaname = 'public'
        ORDER BY n_live_tup DESC
        LIMIT 20;
    "
}

# 함수: 도움말
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --reset   Reset all data and regenerate dummy data"
    echo "  --schema  Create database schema only"
    echo "  --data    Generate dummy data only"
    echo "  --stats   Show database statistics"
    echo "  --help    Show this help message"
    echo ""
    echo "Environment Variables:"
    echo "  DB_HOST      Database host (default: localhost)"
    echo "  DB_PORT      Database port (default: 5432)"
    echo "  DB_NAME      Database name (default: delivery)"
    echo "  DB_USER      Database user (default: delivery)"
    echo "  DB_PASSWORD  Database password (default: secret)"
}

# 메인 로직
main() {
    case "${1:-}" in
        --reset)
            test_connection
            reset_data
            generate_data
            show_stats
            ;;
        --schema)
            test_connection
            create_schema
            ;;
        --data)
            test_connection
            generate_data
            show_stats
            ;;
        --stats)
            test_connection
            show_stats
            ;;
        --help|-h)
            show_help
            ;;
        "")
            test_connection
            create_schema
            generate_data
            show_stats
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
