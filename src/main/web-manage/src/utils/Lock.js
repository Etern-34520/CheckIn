const locks = {};

/*
class ExecutablePromiseWrapper {
    promise;
    constructor(executor) {
        let resolveFunc
        const promise = new Promise((resolve) => {
            resolveFunc = resolve;
        });
        promise.resolve = resolveFunc;
        promise.execute = () => {
            promise.resolve(executor());
        }
        this.promise = promise;
    }
}
*/

const LockUtil = {
    synchronizeExecute: (lockName,executablePromise) => {
        let promise;
        if (locks[lockName] instanceof Array) {
            locks[lockName].push(executablePromise);
        } else {
            locks[lockName] = [];
            executablePromise.then(() => {
                let index = 0;
                const doLoop = () => {
                    if (locks[lockName][index]) {
                        locks[lockName][index].then(() => {
                            index++;
                            doLoop();
                        });
                        locks[lockName][index].execute();
                    } else {
                        locks[lockName] = undefined;
                    }
                }
                doLoop();
            });
            executablePromise.execute();
        }
        return promise;
    }
};
export default LockUtil;