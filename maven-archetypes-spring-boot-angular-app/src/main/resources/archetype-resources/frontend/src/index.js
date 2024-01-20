require('file-loader?name=[name].[ext]!./index.html');

function component() {
  const element = document.createElement('div');
  element.innerHTML = 'Hello! JavaScript is working!';
  return element;
}
document.body.appendChild(component());

