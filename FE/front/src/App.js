import logo from './logo.svg';
import './App.css';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import { Routes, Route, BrowserRouter as Router } from 'react-router-dom';
import LoginLayout from './layout/LoginLayout';
import MainLayout from './layout/MainLayout';
import LoginRedirectPage from './pages/LoginRedirectPage';
import FileDownloadPage from './pages/FileDownloadPage';
import FileManagePage from './pages/FileManagePage';
import { getAccessTokenApi } from './api/getAccessTokenApi';
import { useEffect } from 'react';
import axios from 'axios';

function App() {

  return (
    <div className="App">
      <Router>
        <Routes>
          <Route element={<MainLayout/>}>
            <Route path='/' element={<LandingPage/>}/>
            <Route path='/file' element={<FileManagePage/>}/>
          </Route>
          <Route element={<LoginLayout/>}>
            <Route path='/login' element={<LoginPage/>} />
            <Route path='/auth/:provider/callback' element={<LoginRedirectPage/>}/>
            <Route path='/download/:fileId' element={<FileDownloadPage/>}></Route>
          </Route>
        </Routes>
      </Router>
    </div>
  );
}

export default App;