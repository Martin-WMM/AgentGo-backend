'use strict';

const { test } = require('node:test');
const assert = require('node:assert/strict');
const { validateBranchFlow, validateAncestry, PROJECT_URL } = require('./branch-policy.cjs');

function pr(head, base, source = base, body = '') {
  return {
    head: { ref: head, repo: { full_name: 'Martin-WMM/AgentGo-test' } },
    base: { ref: base, repo: { full_name: 'Martin-WMM/AgentGo-test' } },
    body: 'Closes #7\nProject: ' + PROJECT_URL + '\nSource branch: ' + source + '\n' + body,
  };
}

test('accepts feature and fix integration into their declared release', () => {
  for (const kind of ['feature', 'fix']) {
    assert.equal(validateBranchFlow(pr(kind + '/7-governance', 'release/governance')).sourceBranch, 'release/governance');
  }
});

test('accepts AgentGo cycle release names', () => {
  const branch = 'release/26-TW6-1';
  assert.equal(validateBranchFlow(pr('feature/7-governance', branch)).sourceBranch, branch);
});

test('accepts release branches with the AgentGo cycle naming convention', () => {
  const branch = 'release/26-TM6-1';
  assert.equal(validateBranchFlow(pr('feature/7-governance', branch)).sourceBranch, branch);
});

test('accepts release and hotfix promotion to main', () => {
  for (const branch of ['release/governance', 'hotfix/7-security']) {
    assert.equal(validateBranchFlow(pr(branch, 'main')).sourceBranch, 'main');
  }
});

test('rejects direct feature/fix promotion to main and invalid integration routes', () => {
  for (const [head, base] of [
    ['feature/7-work', 'main'], ['fix/7-work', 'main'],
    ['main', 'release/governance'], ['release/other', 'release/governance'],
    ['hotfix/7-work', 'release/governance'], ['feature/7-work', 'feature/other'],
    ['dependabot/package', 'main'],
  ]) {
    assert.throws(() => validateBranchFlow(pr(head, base)));
  }
});

test('rejects incorrectly declared origins and malformed task branch names', () => {
  assert.throws(() => validateBranchFlow(pr('feature/7-work', 'release/governance', 'main')));
  assert.throws(() => validateBranchFlow(pr('hotfix/7-work', 'main', 'release/governance')));
  assert.throws(() => validateBranchFlow(pr('feature/work', 'release/governance')));
  assert.throws(() => validateBranchFlow(pr('release/', 'main')));
});

test('requires the correct linked issue and exact Project declaration', () => {
  assert.throws(() => validateBranchFlow(pr('fix/8-work', 'release/governance')));
  const missingProject = pr('fix/7-work', 'release/governance');
  missingProject.body = missingProject.body.replace(PROJECT_URL, 'https://example.com');
  assert.throws(() => validateBranchFlow(missingProject));
  const missingIssue = pr('fix/7-work', 'release/governance');
  missingIssue.body = missingIssue.body.replace('Closes #7', 'Related to #7');
  assert.throws(() => validateBranchFlow(missingIssue));
});

test('accepts GitHub closing variants and deduplicates issues', () => {
  const input = pr('feature/7-work', 'release/governance', 'release/governance', 'Fixes #8\nCloses #7');
  assert.deepEqual(validateBranchFlow(input).issueNumbers, [7, 8]);
});

test('rejects cross-repository release and hotfix promotion', () => {
  const input = pr('release/governance', 'main');
  input.head.repo.full_name = 'someone/fork';
  assert.throws(() => validateBranchFlow(input));
});

test('requires the current target commit to be an ancestor of the head', () => {
  for (const status of ['ahead', 'identical']) validateAncestry(status);
  for (const status of ['behind', 'diverged', 'unknown']) {
    assert.throws(() => validateAncestry(status));
  }
});
