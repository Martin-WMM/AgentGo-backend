'use strict';

const PROJECT_URL = 'https://github.com/users/Martin-WMM/projects/2';

function validateBranchFlow(pr) {
  const head = pr.head.ref;
  const base = pr.base.ref;
  const release = /^release\/[A-Za-z0-9][A-Za-z0-9._-]*$/;
  const task = /^(feature|fix|hotfix)\/(\d+)-[a-z0-9][a-z0-9._-]*$/;
  const taskMatch = head.match(task);
  let expectedSource;

  if (base === 'main') {
    if (!release.test(head) && taskMatch?.[1] !== 'hotfix') {
      throw new Error('Only release/* or hotfix/<issue>-<name> may target main.');
    }
    if (pr.head.repo?.full_name !== pr.base.repo.full_name) {
      throw new Error('Release and hotfix promotion must originate in this repository.');
    }
    expectedSource = 'main';
  } else if (release.test(base)) {
    if (!taskMatch || !['feature', 'fix'].includes(taskMatch[1])) {
      throw new Error('Only feature/<issue>-<name> or fix/<issue>-<name> may target release/*.');
    }
    expectedSource = base;
  } else {
    throw new Error('Integration PRs must target main or release/<name>.');
  }

  const body = pr.body || '';
  const source = body.match(/^Source branch:\s*(\S+)\s*$/m)?.[1];
  if (source !== expectedSource) {
    throw new Error('Source branch must be ' + expectedSource + '.');
  }
  const project = body.match(/^Project:\s*(\S+)\s*$/m)?.[1];
  if (project !== PROJECT_URL) {
    throw new Error('Declare AgentGo Project #2 in the PR body.');
  }
  const issueNumbers = [...body.matchAll(/\b(?:closes?|closed|fix(?:es|ed)?|resolves?|resolved)\s+#(\d+)\b/gi)]
    .map((match) => Number(match[1]));
  if (!issueNumbers.length) {
    throw new Error('Link at least one repository Issue using Closes #<number>.');
  }
  if (taskMatch && !issueNumbers.includes(Number(taskMatch[2]))) {
    throw new Error('The branch issue number must be linked by closing syntax.');
  }
  return { sourceBranch: expectedSource, issueNumbers: [...new Set(issueNumbers)] };
}

function validateAncestry(status) {
  if (!['ahead', 'identical'].includes(status)) {
    throw new Error('The head must include the current target branch; update it without a direct protected-branch push.');
  }
}

module.exports = { validateBranchFlow, validateAncestry, PROJECT_URL };
