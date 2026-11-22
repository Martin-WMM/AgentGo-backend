-- Allow auditing CLI executions that were rejected by the command security policy.
ALTER TABLE tbl_cli_execution
    DROP CONSTRAINT IF EXISTS chk_tbl_cli_execution_status;

ALTER TABLE tbl_cli_execution
    ADD CONSTRAINT chk_tbl_cli_execution_status CHECK (
        status IN ('COMPLETED', 'TIMED_OUT', 'FAILED_TO_START', 'REJECTED')
    );

COMMENT ON COLUMN tbl_cli_execution.status IS
    'Execution outcome: COMPLETED, TIMED_OUT, FAILED_TO_START, or REJECTED';
