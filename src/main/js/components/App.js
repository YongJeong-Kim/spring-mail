import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { withStyles, MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import TextField from '@material-ui/core/TextField'
import blue from '@material-ui/core/colors/blue';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import Chip from '@material-ui/core/Chip';

import axios from 'axios';

const styles = theme => ({
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginLeft: theme.spacing.unit,
    marginRight: theme.spacing.unit,
  },
  dense: {
    marginTop: 16,
  },
  col: {
    display: 'flex',
  },
  root: theme.mixins.gutters({
    paddingTop: 16,
    paddingBottom: 16,
    marginTop: theme.spacing.unit * 3,
    maxWidth: '900px',
    margin: '0 auto',
  }),
  row: {
    display: 'flex',
    justifyContent: 'center',
  },
  button: {
    margin: theme.spacing.unit,
  },
  input: {
    display: 'none',
  },
  chip: {
    margin: theme.spacing.unit,
  },
});
const theme = createMuiTheme({
  palette: {
    primary: blue,
  },
  typography: { useNextVariants: true },
});
class App extends Component {
  state = {
    contact: {
      customerEmail: null,
      subject: null,
      text: null,
      file: null,
    },
  };
  filenameUpdate = (file) => {
    this.setState({
      contact: {
        ...this.state.contact,
        file,
      }
    })
  };
  handleFileUpload = (e) => {
    const reader = new FileReader();
    const file = e.target.files[0];
    if (file !== undefined) {
      reader.onloadend = () => {
        this.filenameUpdate(file);
      };
      reader.readAsDataURL(file);
    } else {
      this.filenameUpdate(null);
    }
  };
  handleFileDelete = () => {
    this.fileInput.value = '';
    this.filenameUpdate(null);
  };
  handleClick = () => {
    const mailDTO = Object.assign({}, this.state.contact);

    const getFormData = object => Object.keys(object).reduce((formData, key) => {
      if (object[key] !== null)
        formData.append(key, object[key]);
      return formData;
    }, new FormData());

    const formData = getFormData(mailDTO);

    axios.post("/", formData)
      .then((response) => {
        alert('문의 완료');
      })
      .catch((err) => {
        console.log(err)
      });
  };
  onChangeEmail = (e) => {
    this.setState({
      contact: {
        ...this.state.contact,
        customerEmail: e.target.value,
      }
    })
  };
  onChangeSubject = (e) => {
    this.setState({
      contact: {
        ...this.state.contact,
        subject: e.target.value,
      }
    })
  };
  onChangeText = (e) => {
    this.setState({
      contact: {
        ...this.state.contact,
        text: e.target.value,
      }
    })
  };
  render() {
    const { classes } = this.props;
    const { contact } = this.state;

    return (
      <div className={classes.col} >
        <Paper className={classes.root} elevation={4} style={{width: '100%'}}>
          <MuiThemeProvider theme={theme} >
            <div className={classes.col}>
              <TextField
                id="outlined-email-input"
                label="작성자 이메일"
                className={classes.textField}
                type="email"
                name="customerEmail"
                autoComplete="email"
                margin="normal"
                variant="outlined"
                fullWidth
                onChange={this.onChangeEmail}
              />
            </div>
            <div className={classes.col}>
              <TextField
                id="outlined-subject-input"
                label="제목"
                className={classes.textField}
                name="subject"
                margin="normal"
                variant="outlined"
                fullWidth
                onChange={this.onChangeSubject}
              />
            </div>
            <input
              className={classes.input}
              id="text-button-file"
              multiple
              type="file"
              onChange={this.handleFileUpload}
              ref = {ref => this.fileInput = ref}
            />
            <label htmlFor="text-button-file">
              <Button color='primary' variant='outlined' component="span" className={classes.button}>
                첨부파일
              </Button>
            </label>
            {contact.file && contact.file.name != null &&
              <Chip
                label={contact.file && contact.file.name}
                clickable
                className={classes.chip}
                color="primary"
                variant="outlined"
                onDelete={this.handleFileDelete}
              />
            }
            <div className={classes.col} >
              <TextField
                id="outlined-multiline-static"
                label="내용"
                multiline
                rows='15'
                className={classes.textField}
                margin="normal"
                variant="outlined"
                fullWidth
                onChange={this.onChangeText}
              />
            </div>
            <Button variant="outlined" color="primary" className={classes.button} style={{float: 'right',}} onClick={this.handleClick}>
              {'문의하기'}
            </Button>
          </MuiThemeProvider>
        </Paper>
      </div>
    )
  }
}
App.propTypes = {
  classes: PropTypes.object.isRequired,
};
export default withStyles(styles)(App)