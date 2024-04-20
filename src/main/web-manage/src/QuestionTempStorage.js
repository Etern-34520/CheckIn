import WebSocketConnector from "@/api/websocket.js";

const QuestionTempStorage = {
    questions: {},
    getAsync: (id) => {
        return new Promise((resolve, reject) => {
            let questionInfo = QuestionTempStorage.questions[id];
            if (questionInfo === undefined || questionInfo.simple) {
                WebSocketConnector.send({
                    type: 'getQuestionInfo',
                    questionId: id
                }).then((response) => {
                    // console.log(response);
                    if (questionInfo === undefined) {
                        questionInfo = {};
                    }
                    for (let key in response) {
                        questionInfo[key] = response[key];
                    }
                    questionInfo.simple = false;
                    resolve(questionInfo);
                    /*QuestionTempStorage.questions[id] = response;*/
                });
            } else {
                resolve(questionInfo);
            }
        });
    },
    getContentsAndIdsAsyncByPartitionId: (id) => {
        return new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getQuestionIdAndContentList",
                partitionId: id.toString(),
            }).then((response) => {
                let resolveData = [];
                for (const questionSimpleInfo1 of response.questionList) {
                    if (QuestionTempStorage.questions[questionSimpleInfo1.id] === undefined || QuestionTempStorage.questions[questionSimpleInfo1.id].simple) {
                        const questionSimpleInfo = {};
                        questionSimpleInfo.id = questionSimpleInfo1.id;
                        questionSimpleInfo.content = questionSimpleInfo1.content;
                        questionSimpleInfo.simple = true;
                        QuestionTempStorage.questions[questionSimpleInfo1.id] = questionSimpleInfo;
                        resolveData.push(questionSimpleInfo);
                    } else {
                        resolveData.push(QuestionTempStorage.questions[questionSimpleInfo1.id]);
                    }
                }
                resolve(resolveData);
            }, (error) => {
                reject(error);
            });
        });
    },
    update: (question) => {
        QuestionTempStorage.questions[question.id] = question;
    },
    updateAll: (questions) => {

    },
    add: (question) => {

    },
    delete: (id) => {

    }
}
export default QuestionTempStorage