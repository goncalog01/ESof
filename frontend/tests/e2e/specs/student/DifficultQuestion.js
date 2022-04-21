describe('Student Difficult Question Dashboard Access', () => {
    beforeEach(() => {
        cy.deleteQuestionsAndAnswers();
        //create quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question 1',
            'How many legs does a spider have?',
            '1',
            '2',
            '6',
            '8'
        );
        cy.createQuestion(
            'Question 2',
            'What is the capital of Portugal?',
            'Tomar',
            'Santa Comba Dão',
            'África',
            'Lisboa'
        );
        cy.createQuizzWith2Questions(
            'Quiz Title',
            'Question 1',
            'Question 2'
        );
        cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    })

    it('student answers quizz', () => {
        cy.demoStudentLogin();
        cy.solveQuizz('Quiz Title', 2);
        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});